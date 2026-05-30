# Aplikasi-JEKI
JEKI Store merupakan aplikasi e-commerce pakaian berbasis desktop yang dikembangkan menggunakan Java Swing, JDBC, dan SQL Server sebagai implementasi Tugas Akhir Mata Kuliah Basis Data. Sistem ini dirancang untuk mensimulasikan proses penjualan pakaian secara online, mulai dari login pengguna, pencarian produk berdasarkan nama atau kategori, pengelolaan keranjang belanja, checkout, simulasi pembayaran, hingga pemantauan status pengiriman barang. Selain itu, pengguna juga dapat mengelola informasi profil seperti nomor telepon dan alamat pengiriman.

Aplikasi menyediakan dua jenis akses, yaitu User dan Admin. Pada sisi User, pengguna dapat melihat katalog produk, melakukan transaksi pembelian, serta memantau proses pengiriman pesanan. Sementara itu, Admin dapat mengakses dashboard untuk memantau performa toko melalui informasi total penghasilan, transaksi, produk, dan pelanggan, serta melihat laporan analitik seperti 5 produk terlaris sepanjang waktu, 5 produk terlaris bulan ini, dan 3 produk yang paling sering dibeli bersamaan. Seluruh data dikelola menggunakan database SQL Server dengan penerapan operasi CRUD serta query analitik seperti JOIN, GROUP BY, Subquery, dan fungsi agregasi.

1. **Halaman Login**
Halaman pertama yang ditampilkan ketika aplikasi dijalankan. Halaman ini berfungsi untuk melakukan autentikasi pengguna sebelum dapat mengakses sistem. Pengguna harus memasukkan email dan password yang telah terdaftar pada database. Selain itu, pengguna juga dapat memilih jenis akun yang digunakan, yaitu sebagai User atau Admin. Sistem akan memvalidasi data yang dimasukkan dengan data yang tersimpan pada database. Jika data sesuai, pengguna akan diarahkan ke halaman utama sesuai hak aksesnya. Apabila data tidak sesuai atau terdapat field yang kosong, sistem akan menampilkan pesan kesalahan.

*▫️Fitur yang dapat diakses:*
- Login User
- Login Admin
- Registrasi akun baru
- Validasi email dan password
- Notifikasi kesalahan login

2. **Halaman Home (User)**
Halaman Home merupakan halaman utama yang akan ditampilkan setelah pengguna berhasil login sebagai User. Halaman ini berfungsi sebagai pusat aktivitas pengguna untuk melihat katalog produk yang tersedia. Pada bagian atas terdapat search bar yang digunakan untuk mencari produk berdasarkan nama maupun kategori. Selain itu terdapat tombol keranjang belanja dan profil pengguna. Produk ditampilkan dalam bentuk kartu produk yang berisi gambar, nama produk, harga, dan tombol tambah ke keranjang.

*▫️Fitur yang dapat diakses:*
- Melihat seluruh produk
- Mencari produk berdasarkan nama
- Mencari produk berdasarkan kategori
- Menambahkan produk ke keranjang
- Mengakses halaman profil
- Mengakses halaman keranjang

3. Halaman Detail Produk (Opsional)
Halaman Detail Produk digunakan untuk menampilkan informasi lengkap mengenai suatu produk yang dipilih pengguna. Informasi yang ditampilkan meliputi gambar produk, ID produk, nama produk, kategori, dan harga. Halaman ini membantu pengguna memperoleh informasi lebih rinci sebelum melakukan pembelian.

*▫️Fitur yang dapat diakses:*
- Menambahkan produk ke keranjang
- Kembali ke halaman Home

4. Halaman Keranjang Belanja (Shopping Cart)
Halaman Keranjang Belanja berfungsi untuk menampilkan seluruh produk yang telah dipilih oleh pengguna sebelum melakukan checkout. Pada halaman ini pengguna dapat melihat daftar produk, jumlah barang yang dibeli, harga satuan, subtotal, dan total belanja. Pengguna juga dapat menghapus produk yang tidak jadi dibeli atau mengubah jumlah pembelian.

*▫️Fitur yang dapat diakses:*
- Melihat daftar produk yang dipilih
- Menambah jumlah produk
- Mengurangi jumlah produk
- Menghapus produk dari keranjang
- Melihat total pembayaran
- Melanjutkan ke proses checkout

5. Halaman Checkout dan Pembayaran
Halaman Checkout dan Pembayaran digunakan untuk menyelesaikan proses transaksi pembelian. Pada halaman ini sistem menampilkan detail barang yang dibeli beserta ringkasan pembayaran. Pengguna dapat memilih jasa pengiriman yang tersedia dan memasukkan nominal pembayaran. Sistem akan menghitung subtotal, ongkos kirim, total pembayaran, serta kembalian yang diterima pengguna.

*▫️Fitur yang dapat diakses:*
- Melihat detail pesanan
- Memilih jasa pengiriman
- Melihat subtotal dan total pembayaran
- Melakukan simulasi pembayaran
- Menyelesaikan transaksi
- Menampilkan bukti pembayaran

6. Halaman Status Pengiriman (Shipment)
Halaman Status Pengiriman digunakan untuk memantau proses pengiriman barang setelah transaksi berhasil dilakukan. Sistem akan menampilkan informasi mengenai status pesanan mulai dari proses pengemasan hingga barang diterima pelanggan. Selain itu ditampilkan juga detail pengiriman seperti nomor resi, jasa kirim, dan informasi pembayaran.

Fitur yang dapat diakses:
Melihat status pengiriman
Melihat nomor resi
Melihat jasa pengiriman
Melihat detail pesanan
Melihat riwayat pembayaran
Kembali ke halaman utama
7. Halaman Profil Pengguna

Halaman Profil digunakan untuk menampilkan informasi akun pengguna yang tersimpan di database. Pada halaman ini pengguna dapat melihat nama, email, nomor telepon, dan alamat. Sesuai kebutuhan sistem, username dan email hanya dapat dilihat, sedangkan nomor telepon dan alamat dapat diperbarui oleh pengguna.

Fitur yang dapat diakses:
Melihat data akun
Mengubah nomor telepon
Mengubah alamat
Menyimpan perubahan profil
Kembali ke halaman Home
8. Dashboard Admin

Dashboard Admin merupakan halaman utama yang ditampilkan setelah administrator berhasil login. Halaman ini berfungsi untuk memantau kondisi toko secara keseluruhan. Dashboard menampilkan berbagai informasi penting seperti total penghasilan, total transaksi, total produk, dan total pelanggan. Selain itu terdapat beberapa laporan analitik yang membantu admin dalam melakukan evaluasi penjualan.

Fitur yang dapat diakses:
Melihat total penghasilan
Melihat total transaksi
Melihat total produk
Melihat total pelanggan
Mengakses laporan penjualan
9. Section 1 – 5 Produk Terlaris Sepanjang Waktu

Section ini digunakan untuk menampilkan lima produk dengan jumlah penjualan tertinggi sejak sistem digunakan. Data ditampilkan dalam bentuk tabel yang berisi ranking, nama produk, total penjualan, dan total pendapatan yang dihasilkan.

Tujuan:
Mengetahui produk paling populer
Membantu pengambilan keputusan stok
Membantu strategi promosi produk
10. Section 2 – 5 Produk Terlaris Bulan Ini

Section ini menampilkan lima produk dengan penjualan tertinggi dalam periode satu bulan terakhir. Data yang ditampilkan terdiri dari ranking, nama produk, jumlah terjual, dan kategori produk.

Tujuan:
Melihat tren penjualan terbaru
Mengetahui produk yang sedang diminati pelanggan
Membantu evaluasi penjualan bulanan
11. Section 3 – Produk yang Sering Dibeli Bersama

Section ini menampilkan tiga kombinasi produk yang paling sering dibeli secara bersamaan oleh pelanggan. Data diperoleh dari analisis transaksi yang telah tersimpan pada database.

Tujuan:
Mengetahui pola pembelian pelanggan
Membantu strategi bundling produk
Membantu pembuatan promo kombinasi produk
Ringkasan Hak Akses User

User dapat mengakses:

Login
Registrasi
Home
Search Produk
Keranjang Belanja
Checkout
Pembayaran
Status Pengiriman
Profil
Ringkasan Hak Akses Admin

Admin dapat mengakses:

Login Admin
Dashboard
Statistik Penjualan
Analisis Produk Terlaris
Analisis Produk yang Dibeli Bersamaan
Monitoring Data Penjualan
Monitoring Pelanggan
Monitoring Produk

Dengan pembagian tersebut, sistem memenuhi kebutuhan tugas akhir Basis Data karena mencakup autentikasi pengguna, pencarian data, simulasi transaksi, pembayaran, pengiriman, pengelolaan profil, serta penyajian laporan analitik yang memanfaatkan query SQL seperti JOIN, GROUP BY, Subquery, dan fungsi agregasi.
