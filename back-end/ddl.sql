CREATE DATABASE Uniqlo;
USE Uniqlo;

-- ======================
-- CUSTOMER
-- ======================

CREATE TABLE Customer (
    id_customer VARCHAR(50) PRIMARY KEY,
    nama VARCHAR(100),
    email VARCHAR(100),
    alamat TEXT,
    no_telepon VARCHAR(20)
);

-- ======================
-- ADMIN
-- ======================

CREATE TABLE Admin (
    id_admin VARCHAR(50) PRIMARY KEY,
    nama_admin VARCHAR(100),
    email VARCHAR(100),
    password VARCHAR(100),
    no_hp VARCHAR(20)
);

-- ======================
-- CATEGORY
-- ======================

CREATE TABLE Category (
    id_category VARCHAR(50) PRIMARY KEY,
    nama_kategori VARCHAR(100)
);

-- ======================
-- PRODUCT
-- ======================

CREATE TABLE Product (
    id_product VARCHAR(50) PRIMARY KEY,
    nama_produk VARCHAR(200),
    deskripsi TEXT,
    harga DECIMAL(12,2),
    stok INT,
    ukuran VARCHAR(50),
    warna VARCHAR(50)
);

-- ======================
-- PRODUCT CATEGORY
-- ======================

CREATE TABLE Product_Category (
    id_product VARCHAR(50),
    id_category VARCHAR(50),

    PRIMARY KEY (id_product, id_category),

    FOREIGN KEY (id_product)
    REFERENCES Product(id_product),

    FOREIGN KEY (id_category)
    REFERENCES Category(id_category)
);

-- ======================
-- ORDERS
-- ======================

CREATE TABLE Orders (
    id_order VARCHAR(50) PRIMARY KEY,
    tanggal_order DATE,
    subtotal DECIMAL(12,2),
    status_order VARCHAR(100)
);

-- ======================
-- ORDER DETAIL
-- ======================

CREATE TABLE OrderDetail (
    id_order VARCHAR(50),
    id_product VARCHAR(50),
    harga DECIMAL(12,2),
    jumlah INT,
    subtotal DECIMAL(12,2),

    PRIMARY KEY (id_order, id_product),

    FOREIGN KEY (id_order)
    REFERENCES Orders(id_order),

    FOREIGN KEY (id_product)
    REFERENCES Product(id_product)
);

-- ======================
-- PAYMENT
-- ======================

CREATE TABLE Payment (
    id_payment VARCHAR(50) PRIMARY KEY,
    alamat TEXT,
    metode_pembayaran VARCHAR(100),
    tanggal_pembayaran DATE,
    jasa_kirim VARCHAR(100),
    status_pembayaran VARCHAR(100),
    subtotal DECIMAL(12,2)
);

-- ======================
-- INVOICES
-- ======================

CREATE TABLE Invoices (
    id_invoices VARCHAR(50) PRIMARY KEY,
    tanggal_pesan DATE,
    alamat TEXT,
    subtotal DECIMAL(12,2),
    status_pembayaran VARCHAR(100),
    nama_jasa_kirim VARCHAR(100)
);

-- ======================
-- SHIPMENT
-- ======================

CREATE TABLE Shipment (
    id_shipment VARCHAR(50) PRIMARY KEY,
    id_order VARCHAR(50),
    alamat TEXT,
    tanggal_kirim DATE,
    status_kirim VARCHAR(100),
    nama_jasa_kirim VARCHAR(100),
    resi VARCHAR(100),
    ongkir DECIMAL(12,2),

    FOREIGN KEY (id_order)
    REFERENCES Orders(id_order)
);