# SatgasKeamananAPI/urls.py
from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static

# Import JWT views
from rest_framework_simplejwt.views import (
    TokenObtainPairView, # Digunakan untuk login (mendapatkan token pertama)
    TokenRefreshView,    # Digunakan untuk memperbarui access token
)

urlpatterns = [
    path('admin/', admin.site.urls),

    # 1. JWT Authentication Endpoints
    path('api/token/', TokenObtainPairView.as_view(), name='token_obtain_pair'),
    path('api/token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),

    # 2. Aplikasi Core API
    path('api/', include('core.urls')),
]

# ... Bagian media files (pastikan ini ada) ...
if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)