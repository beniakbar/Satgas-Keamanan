# core/serializers.py
from rest_framework import serializers
from .models import User, Presensi, Laporan
from rest_framework import serializers
from .models import User, Presensi
from django.utils import timezone
from datetime import date # Import date untuk perbandingan tanggal

class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ['id', 'first_name', 'last_name', 'email', 'phone_number', 'profile_picture', 'last_login', 'is_active', 'is_admin', 'is_petugas']
        read_only_fields = ['is_admin', 'is_petugas', 'last_login']

class PresensiSerializer(serializers.ModelSerializer):
    petugas_name = serializers.CharField(source='petugas.first_name', read_only=True)

    class Meta:
        model = Presensi
        fields = ['id', 'petugas', 'petugas_name', 'timestamp', 'latitude', 'longitude', 'location_note', 'note', 'selfie_photo']
        read_only_fields = ['petugas', 'timestamp'] # Petugas di-set otomatis di View

class LaporanSerializer(serializers.ModelSerializer):
    petugas_name = serializers.CharField(source='petugas.first_name', read_only=True)

    class Meta:
        model = Laporan
        fields = ['id', 'petugas', 'petugas_name', 'timestamp', 'latitude', 'longitude', 'location_note', 'note', 'photo', 'status', 'priority']
        read_only_fields = ['petugas', 'timestamp', 'status', 'priority'] # Status/Priority diurus Admin


# Serializer untuk Admin melihat detail Petugas
class PetugasDetailSerializer(serializers.ModelSerializer):
    # Field 'timestamp' adalah nama field di Presensi model
    # source='presensi_set.last' akan mencoba mendapatkan objek Presensi terakhir
    # Namun, lebih baik menggunakan method custom untuk logic kompleks
    
    # Kita akan membuat property di model User, lalu panggil di serializer
    # last_presensi_date = serializers.SerializerMethodField()
    
    class Meta:
        model = User
        fields = [
            'id', 'first_name', 'last_name', 'email', 'phone_number', 
            'profile_picture', 'is_active', 'last_login', 'date_joined'
        ]
        # Admin perlu melihat semua field ini
        read_only_fields = ['email', 'is_active', 'last_login', 'date_joined']

class PetugasStatusPresensiSerializer(serializers.ModelSerializer):
    # Field kustom untuk menunjukkan apakah petugas sudah presensi hari ini
    has_presensi_today = serializers.SerializerMethodField()
    
    # Detail presensi terakhir (jika ada)
    last_presensi = serializers.SerializerMethodField()

    class Meta:
        model = User
        fields = [
            'id', 'first_name', 'last_name', 'email', 
            'phone_number', 'has_presensi_today', 'last_presensi'
        ]

    def get_has_presensi_today(self, obj):
        # Mengambil tanggal hari ini (berdasarkan timezone settings Django)
        today = timezone.localdate()
        
        # Mengecek apakah ada objek Presensi untuk petugas ini pada hari ini
        return Presensi.objects.filter(
            petugas=obj, 
            timestamp__date=today
        ).exists()

    def get_last_presensi(self, obj):
        # Mendapatkan objek Presensi terakhir (jika ada)
        try:
            last_presensi = Presensi.objects.filter(petugas=obj).latest('timestamp')
            # Gunakan serializer yang sudah ada untuk detail presensi
            return AdminPresensiSerializer(last_presensi).data
        except Presensi.DoesNotExist:
            return None

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
