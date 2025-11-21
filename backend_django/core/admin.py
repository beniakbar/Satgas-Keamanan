# core/admin.py
from django.contrib import admin
from .models import User, Presensi, Laporan

admin.site.register(User)
admin.site.register(Presensi)
admin.site.register(Laporan)