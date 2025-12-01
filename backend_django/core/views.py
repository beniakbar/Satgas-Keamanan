# core/views.py
from rest_framework import viewsets, permissions, status, serializers
from rest_framework.response import Response
from rest_framework.parsers import MultiPartParser, FormParser
from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated

from rest_framework.permissions import AllowAny
from django.contrib.auth import get_user_model
User = get_user_model()

from django.utils import timezone
from django.utils.dateparse import parse_date

from .models import User, Presensi, Laporan
from .serializers import (
    PetugasDetailSerializer,
    AdminPresensiSerializer,
    AdminLaporanSerializer,
    PresensiSerializer,
    LaporanSerializer,
    EmailTokenObtainPairSerializer,
    UserSerializer,
    PetugasStatusPresensiSerializer
)

from rest_framework_simplejwt.views import TokenObtainPairView


class RegisterUserView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        email = request.data.get("email")
        password = request.data.get("password")

        if not email or not password:
            return Response({"error": "Email dan password wajib diisi."},
                            status=status.HTTP_400_BAD_REQUEST)

        if User.objects.filter(email=email).exists():
            return Response({"error": "Email sudah terdaftar."},
                            status=status.HTTP_400_BAD_REQUEST)

        user = User.objects.create_user(
            email=email,
            password=password,
        )

        return Response(
            {"message": "User berhasil dibuat."},
            status=status.HTTP_201_CREATED
        )

# ============================================
# 1. CUSTOM PERMISSIONS
# ============================================

class IsPetugas(permissions.BasePermission):
    """Izinkan akses hanya untuk Petugas."""
    def has_permission(self, request, view):
        return bool(request.user and request.user.is_authenticated and request.user.is_petugas)


class IsAdmin(permissions.BasePermission):
    """Izinkan akses hanya untuk Admin."""
    def has_permission(self, request, view):
        return bool(request.user and request.user.is_authenticated and request.user.is_admin)


# ============================================
# 2. ADMIN - Lihat Petugas
# ============================================

class PetugasViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = User.objects.filter(is_petugas=True).order_by('email')
    serializer_class = PetugasDetailSerializer
    permission_classes = [IsAdmin]


# ============================================
# 3. ADMIN - Monitoring Presensi
# ============================================

class AdminPresensiViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = Presensi.objects.all().order_by('-timestamp')
    serializer_class = AdminPresensiSerializer
    permission_classes = [IsAdmin]


# ============================================
# 4. ADMIN - Monitoring & Update Status Laporan
# ============================================

class AdminLaporanViewSet(viewsets.ModelViewSet):
    queryset = Laporan.objects.all().order_by('-timestamp')
    serializer_class = AdminLaporanSerializer
    permission_classes = [IsAdmin]

    def create(self, request, *args, **kwargs):
        return Response(
            {'detail': 'Laporan hanya bisa dibuat oleh Petugas.'},
            status=status.HTTP_403_FORBIDDEN
        )


# ============================================
# 5. PETUGAS - PRESENSI
# ============================================

class PresensiViewSet(viewsets.ModelViewSet):
    serializer_class = PresensiSerializer
    permission_classes = [IsAuthenticated]
    parser_classes = [MultiPartParser, FormParser]

    def get_queryset(self):
        user = self.request.user
        if user.is_admin:
            return Presensi.objects.all().order_by('-timestamp')
        if user.is_petugas:
            return Presensi.objects.filter(petugas=user).order_by('-timestamp')
        return Presensi.objects.none()

    def perform_create(self, serializer):
        if not self.request.user.is_petugas:
            raise permissions.exceptions.PermissionDenied("Hanya Petugas yang dapat membuat presensi.")

        today = timezone.localdate()

        if Presensi.objects.filter(petugas=self.request.user, timestamp__date=today).exists():
            raise serializers.ValidationError("Anda sudah melakukan presensi harian hari ini.")

        serializer.save(petugas=self.request.user)


# ============================================
# 6. ADMIN - LAPORAN HARIAN PRESENSI
# ============================================

class HarianPresensiReportView(APIView):
    permission_classes = [IsAdmin]

    def get(self, request):
        # Ambil parameter 'date' dari query string
        date_str = request.query_params.get('date')
        target_date = timezone.localdate() # Default hari ini

        if date_str:
            parsed_date = parse_date(date_str)
            if parsed_date:
                target_date = parsed_date
            else:
                return Response({'error': 'Format tanggal salah. Gunakan YYYY-MM-DD'}, status=status.HTTP_400_BAD_REQUEST)

        petugas_queryset = User.objects.filter(is_petugas=True).order_by('email')
        
        # PERBAIKAN: Sertakan 'request' dalam context agar ImageField dapat membuat URL absolut
        serializer_context = {
            'request': request,
            'target_date': target_date
        }
        
        serializer = PetugasStatusPresensiSerializer(
            petugas_queryset, 
            many=True, 
            context=serializer_context
        )

        data = serializer.data
        total_petugas = len(data)
        hadir = sum(1 for p in data if p.get('has_presensi_today'))

        return Response({
            'report_date': target_date.isoformat(),
            'total_petugas': total_petugas,
            'petugas_hadir': hadir,
            'petugas_belum_hadir': total_petugas - hadir,
            'data': data
        })


# ============================================
# 7. PETUGAS - LAPORAN
# ============================================

class LaporanViewSet(viewsets.ModelViewSet):
    serializer_class = LaporanSerializer
    permission_classes = [IsAuthenticated]
    parser_classes = [MultiPartParser, FormParser]

    def get_queryset(self):
        user = self.request.user
        if user.is_admin:
            return Laporan.objects.all().order_by('-timestamp')
        if user.is_petugas:
            return Laporan.objects.filter(petugas=user).order_by('-timestamp')
        return Laporan.objects.none()

    def perform_create(self, serializer):
        if not self.request.user.is_petugas:
            raise permissions.exceptions.PermissionDenied("Hanya Petugas yang dapat membuat laporan.")

        serializer.save(petugas=self.request.user)


# ============================================
# 8. LOGIN EMAIL JWT
# ============================================

class EmailTokenObtainPairView(TokenObtainPairView):
    serializer_class = EmailTokenObtainPairSerializer


# ============================================
# 9. USER PROFILE
# ============================================

class UserProfileView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        serializer = UserSerializer(request.user)
        return Response(serializer.data)
