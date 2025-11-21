from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import UserProfileView 
from .views import RegisterUserView

from .views import (
    PresensiViewSet,
    LaporanViewSet,
    PetugasViewSet,
    AdminPresensiViewSet,
    AdminLaporanViewSet,
    HarianPresensiReportView,
)

router = DefaultRouter()

# Endpoint Petugas (Presensi & Laporan)
router.register(r'presensi', PresensiViewSet, basename='presensi')
router.register(r'laporan', LaporanViewSet, basename='laporan')


# Endpoint Admin → Data Petugas
router.register(r'admin/petugas', PetugasViewSet, basename='admin-petugas')

# Endpoint Admin → Monitoring Presensi & Laporan
router.register(r'admin/presensi', AdminPresensiViewSet, basename='admin-presensi')
router.register(r'admin/laporan', AdminLaporanViewSet, basename='admin-laporan')


urlpatterns = [
    path('', include(router.urls)),

    # Admin: laporan presensi harian
    path(
        'admin/laporan/harian/',
        HarianPresensiReportView.as_view(),
        name='harian-presensi-report'
    ),
    
    # PERBAIKAN KRITIS: Tambahkan 'user/' di sini agar cocok dengan permintaan klien Android
    path('user/profile/', UserProfileView.as_view(), name='user-profile'), 

    path('user/register/', RegisterUserView.as_view(), name='user-register'),
]