# core/models.py
from django.db import models
from django.contrib.auth.models import AbstractUser


# Fungsi untuk menentukan lokasi upload foto
def user_directory_path(instance, filename):
    return 'user_{0}/{1}'.format(instance.email, filename)

from django.contrib.auth.base_user import BaseUserManager

class UserManager(BaseUserManager):
    use_in_migrations = True

    def create_user(self, email, password=None, **extra_fields):
        if not email:
            raise ValueError("Email harus diisi")

        email = self.normalize_email(email)
        user = self.model(email=email, **extra_fields)
        user.set_password(password)
        user.save(using=self._db)
        return user

    def create_superuser(self, email, password=None, **extra_fields):
        extra_fields.setdefault("is_staff", True)
        extra_fields.setdefault("is_superuser", True)
        extra_fields.setdefault("is_admin", True)        # OPTIONAL: jika admin app Anda pakai ini
        extra_fields.setdefault("is_petugas", False)     # OPTIONAL

        if extra_fields.get("is_staff") is not True:
            raise ValueError("Superuser harus memiliki is_staff=True.")

        if extra_fields.get("is_superuser") is not True:
            raise ValueError("Superuser harus memiliki is_superuser=True.")

        return self.create_user(email, password, **extra_fields)


class User(AbstractUser):
    username = None
    email = models.EmailField(unique=True)
    phone_number = models.CharField(max_length=15, blank=True, null=True)
    profile_picture = models.ImageField(upload_to=user_directory_path, blank=True, null=True)

    is_admin = models.BooleanField(default=False)
    is_petugas = models.BooleanField(default=True)

    USERNAME_FIELD = 'email'
    REQUIRED_FIELDS = []

    objects = UserManager()   # <--- Tambahkan ini

    def __str__(self):
        return self.email


class Presensi(models.Model):
    petugas = models.ForeignKey(User, on_delete=models.CASCADE, related_name='presensi_set')
    timestamp = models.DateTimeField(auto_now_add=True)
    latitude = models.DecimalField(max_digits=9, decimal_places=6)
    longitude = models.DecimalField(max_digits=9, decimal_places=6)
    location_note = models.CharField(max_length=255, help_text="Alamat atau nama lokasi presensi.")
    note = models.TextField(help_text="Catatan harian dari petugas (e.g., 'Presensi Harian').")
    selfie_photo = models.ImageField(upload_to='presensi_photos/')

    class Meta:
        # Menjamin hanya 1 presensi per petugas per hari
        # unique_together = ('petugas', 'timestamp') # Ini harus diubah di level view/logic, bukan DB
        # HAPUS BARIS DI ATAS, atau ganti dengan:
        pass

    def __str__(self):
        return f"Presensi {self.petugas.first_name} pada {self.timestamp.date()}"


class Laporan(models.Model):
    petugas = models.ForeignKey(User, on_delete=models.CASCADE, related_name='laporan_set')
    timestamp = models.DateTimeField(auto_now_add=True)
    latitude = models.DecimalField(max_digits=9, decimal_places=6)
    longitude = models.DecimalField(max_digits=9, decimal_places=6)
    location_note = models.CharField(max_length=255, help_text="Alamat atau nama lokasi kejadian.")
    note = models.TextField(help_text="Detail laporan insiden/anomaly.")
    photo = models.ImageField(upload_to='laporan_photos/')

    # Tambahan: Status dan Prioritas Laporan
    STATUS_CHOICES = [
        ('open', 'Open'),
        ('in_progress', 'In Progress'),
        ('closed', 'Closed'),
    ]
    PRIORITY_CHOICES = [
        ('low', 'Low'),
        ('medium', 'Medium'),
        ('high', 'High'),
    ]

    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='open')
    priority = models.CharField(max_length=20, choices=PRIORITY_CHOICES, default='medium')

    def __str__(self):
        return f"Laporan {self.petugas.first_name} - {self.status}"