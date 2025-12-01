# core/serializers.py
from rest_framework import serializers
from .models import User, Presensi, Laporan
from django.utils import timezone

class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['id', 'first_name', 'last_name', 'email', 'phone_number', 'profile_picture', 'last_login', 'is_active', 'is_admin', 'is_petugas']
        read_only_fields = ['is_admin', 'is_petugas', 'last_login']

class PresensiSerializer(serializers.ModelSerializer):
    petugas_name = serializers.CharField(source='petugas.first_name', read_only=True)

    class Meta:
        model = Presensi
        fields = [
            'id', 'petugas', 'petugas_name', 'timestamp',
            'latitude', 'longitude', 'location_note',
            'note', 'selfie_photo'
        ]
        read_only_fields = ['petugas', 'timestamp']

        extra_kwargs = {
            'location_note': {'required': False, 'allow_blank': True},
            'note': {'required': False, 'allow_blank': True},
        }

class LaporanSerializer(serializers.ModelSerializer):
    petugas_name = serializers.CharField(source='petugas.first_name', read_only=True)

    class Meta:
        model = Laporan
        fields = ['id', 'petugas', 'petugas_name', 'timestamp', 'latitude', 'longitude', 'location_note', 'note', 'photo', 'status', 'priority']
        read_only_fields = ['petugas', 'timestamp', 'status', 'priority'] # Status/Priority diurus Admin


# Serializer untuk Admin melihat detail Petugas
class PetugasDetailSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = [
            'id', 'first_name', 'last_name', 'email', 'phone_number', 
            'profile_picture', 'is_active', 'last_login', 'date_joined'
        ]
        read_only_fields = ['email', 'is_active', 'last_login', 'date_joined']

# Serializer untuk Admin melihat Daftar Presensi
class AdminPresensiSerializer(serializers.ModelSerializer):
    # Mengambil nama petugas dari Foreign Key
    petugas_name = serializers.CharField(source='petugas.first_name', read_only=True)
    petugas_email = serializers.EmailField(source='petugas.email', read_only=True)

    class Meta:
        model = Presensi
        fields = [
            'id', 'petugas_name', 'petugas_email', 'timestamp', 
            'latitude', 'longitude', 'location_note', 'note', 'selfie_photo'
        ]

class PetugasStatusPresensiSerializer(serializers.ModelSerializer):
    # Field kustom untuk menunjukkan apakah petugas sudah presensi hari ini
    has_presensi_today = serializers.SerializerMethodField()
    
    # Detail presensi terakhir (jika ada)
    last_presensi = serializers.SerializerMethodField()
    
    # Tambahkan full_name agar sesuai dengan Model Android
    full_name = serializers.SerializerMethodField()

    class Meta:
        model = User
        fields = [
            'id', 'first_name', 'last_name', 'full_name',
            'email', 'phone_number', 'has_presensi_today', 'last_presensi'
        ]

    def get_full_name(self, obj):
        full = f"{obj.first_name} {obj.last_name}".strip()
        return full if full else obj.email 

    def get_target_date(self):
        """Helper untuk mengambil tanggal target dari context atau default hari ini"""
        return self.context.get('target_date', timezone.localdate())

    def get_has_presensi_today(self, obj):
        # Gunakan tanggal target dari context
        target_date = self.get_target_date()
        
        return Presensi.objects.filter(
            petugas=obj, 
            timestamp__date=target_date
        ).exists()

    def get_last_presensi(self, obj):
        # Untuk laporan historis, kita ingin melihat presensi PADA tanggal tersebut
        target_date = self.get_target_date()
        
        try:
            # Gunakan order_by().first() yang lebih aman daripada latest()
            presensi_on_date = Presensi.objects.filter(
                petugas=obj, 
                timestamp__date=target_date
            ).order_by('-timestamp').first()
            
            if presensi_on_date:
                # PENTING: Teruskan self.context agar 'request' ikut terbawa ke serializer anak
                return AdminPresensiSerializer(presensi_on_date, context=self.context).data
            return None
            
        except Exception:
            return None


# Serializer untuk Admin melihat Daftar Laporan
class AdminLaporanSerializer(serializers.ModelSerializer):
    petugas_name = serializers.CharField(source='petugas.first_name', read_only=True)
    petugas_email = serializers.EmailField(source='petugas.email', read_only=True)

    class Meta:
        model = Laporan
        fields = [
            'id', 'petugas_name', 'petugas_email', 'timestamp', 
            'latitude', 'longitude', 'location_note', 'note', 'photo', 
            'status', 'priority'
        ]
        read_only_fields = ['status', 'priority'] # Admin bisa mengupdate status/priority

# Tambahkan di bawah serializer lain
from rest_framework_simplejwt.serializers import TokenObtainPairSerializer

class EmailTokenObtainPairSerializer(TokenObtainPairSerializer):
    """Serializer login JWT menggunakan email sebagai kredensial."""
    username_field = 'email'
