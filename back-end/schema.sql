-- ============================================================
-- SQL DDL untuk Fitur Cart & Pembayaran (SQL Server)
-- Jalankan ini pada database SQL Server Anda (Uniqlo)
-- ============================================================

USE Uniqlo;
GO
-- 2. Buat ulang tabel Cart baru dengan struktur yang sudah sesuai (memiliki total_harga)
CREATE TABLE Cart (
    id_cart INT IDENTITY(1,1) PRIMARY KEY,
    id_customer VARCHAR(50) NOT NULL,
    id_product VARCHAR(50) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    total_harga DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    
    CONSTRAINT FK_Cart_Customer 
        FOREIGN KEY (id_customer) 
        REFERENCES Customer(id_customer)
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
        
    CONSTRAINT FK_Cart_Product 
        FOREIGN KEY (id_product) 
        REFERENCES Product(id_product)
        ON DELETE CASCADE 
        ON UPDATE CASCADE
);
GO

-- ──────────────────────────────────────────────────────────
-- 2. TABEL TRANSACTION (Pencatatan Transaksi)
-- ──────────────────────────────────────────────────────────
CREATE TABLE Transaction (
        id_transaction INT IDENTITY(1,1) PRIMARY KEY,
        id_customer VARCHAR(50) NOT NULL,
        total_belanja DECIMAL(12,2) NOT NULL,
        uang_dibayar DECIMAL(12,2) NOT NULL,
        kembalian DECIMAL(12,2) NOT NULL,
        tanggal_transaksi DATETIME NOT NULL,
        
        CONSTRAINT FK_Transaction_Customer 
            FOREIGN KEY (id_customer) 
            REFERENCES Customer(id_customer)
            ON DELETE CASCADE 
            ON UPDATE CASCADE
    );

    SELECT * FROM Transaction


-- ──────────────────────────────────────────────────────────
-- 3. PERUBAHAN TABEL ADMIN (Menambah Saldo)
-- ──────────────────────────────────────────────────────────

-- Menambahkan kolom saldo ke tabel Admin jika belum ada
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[Admin]') AND name = 'saldo')
BEGIN
    ALTER TABLE Admin ADD saldo DECIMAL(12,2) DEFAULT 0.00;
END
GO

-- Memasukkan default Admin (A001) agar proses update saldo saat transaksi berhasil berjalan lancar
IF NOT EXISTS (SELECT * FROM Admin WHERE id_admin = 'A001')
BEGIN
    INSERT INTO Admin (id_admin, nama_admin, email, password, no_hp, saldo)
    VALUES ('A001', 'Admin Utama', 'admin@uniqlo.com', 'admin123', '08123456789', 0.00);
END
GO
