-- ============================================================
--  DDL  –  JEKI Store  |  Database: Uniqlo
--  Tabel: Cart, CartItem, Orders
--  Jalankan setelah tabel Customer sudah ada.
-- ============================================================

-- ──────────────────────────────────────────────────────────
--  1. CART
--     Menyimpan satu sesi keranjang per transaksi checkout.
-- ──────────────────────────────────────────────────────────
CREATE TABLE Cart (
    id_cart        INT           NOT NULL IDENTITY(1,1) PRIMARY KEY,
    id_customer    VARCHAR(50)   NOT NULL,
    tanggal_buat   DATETIME      NOT NULL DEFAULT GETDATE(),
    kode_promo     VARCHAR(50)   NULL,        -- kode promo yg dipakai (boleh NULL)
    total_harga    BIGINT        NOT NULL DEFAULT 0,

    CONSTRAINT FK_Cart_Customer
        FOREIGN KEY (id_customer)
        REFERENCES Customer(id_customer)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ──────────────────────────────────────────────────────────
--  2. CARTITEM
--     Detail setiap produk di dalam satu Cart.
-- ──────────────────────────────────────────────────────────
CREATE TABLE CartItem (
    id_cart_item   INT           NOT NULL IDENTITY(1,1) PRIMARY KEY,
    id_cart        INT           NOT NULL,
    id_produk      VARCHAR(10)   NOT NULL,    -- P001, P002, …
    nama_produk    NVARCHAR(200) NOT NULL,
    harga_satuan   BIGINT        NOT NULL,    -- dalam Rupiah (tanpa titik)
    jumlah         INT           NOT NULL DEFAULT 1,
    ukuran         VARCHAR(10)   NOT NULL DEFAULT 'M',  -- S / M / L / XL / XXL

    CONSTRAINT FK_CartItem_Cart
        FOREIGN KEY (id_cart)
        REFERENCES Cart(id_cart)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ──────────────────────────────────────────────────────────
--  3. ORDERS
--     Satu order dibuat saat user klik "CHECKOUT NOW".
-- ──────────────────────────────────────────────────────────
CREATE TABLE Orders (
    id_order           INT           NOT NULL IDENTITY(1,1) PRIMARY KEY,
    id_cart            INT           NOT NULL,
    id_customer        VARCHAR(50)   NOT NULL,
    metode_pembayaran  NVARCHAR(50)  NOT NULL,  -- QRIS / Transfer Bank / dll.
    status_order       NVARCHAR(30)  NOT NULL DEFAULT 'Pending',
        -- nilai: Pending | Diproses | Dikirim | Selesai | Dibatalkan
    tanggal_order      DATETIME      NOT NULL DEFAULT GETDATE(),

    CONSTRAINT FK_Orders_Cart
        FOREIGN KEY (id_cart)
        REFERENCES Cart(id_cart),

    CONSTRAINT FK_Orders_Customer
        FOREIGN KEY (id_customer)
        REFERENCES Customer(id_customer)
);

-- ──────────────────────────────────────────────────────────
--  INDEX (opsional, mempercepat query by customer)
-- ──────────────────────────────────────────────────────────
CREATE INDEX IX_Cart_Customer    ON Cart(id_customer);
CREATE INDEX IX_Orders_Customer  ON Orders(id_customer);
CREATE INDEX IX_Orders_Cart      ON Orders(id_cart);

-- ──────────────────────────────────────────────────────────
--  CONTOH QUERY untuk melihat pesanan beserta itemnya
-- ──────────────────────────────────────────────────────────
/*
SELECT
    o.id_order,
    o.id_customer,
    o.metode_pembayaran,
    o.status_order,
    o.tanggal_order,
    c.kode_promo,
    c.total_harga,
    ci.id_produk,
    ci.nama_produk,
    ci.harga_satuan,
    ci.jumlah,
    ci.ukuran,
    (ci.harga_satuan * ci.jumlah) AS subtotal_item
FROM Orders   o
JOIN Cart     c  ON o.id_cart     = c.id_cart
JOIN CartItem ci ON ci.id_cart    = c.id_cart
WHERE o.id_customer = 'C001'
ORDER BY o.tanggal_order DESC;
*/
