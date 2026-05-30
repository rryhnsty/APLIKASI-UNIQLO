USE Uniqlo;
GO


SELECT TOP 5
    p.id_product,
    p.nama_produk,
    SUM(od.jumlah)          AS total_terjual,
    SUM(od.subtotal)        AS total_pendapatan
FROM OrderDetail od
JOIN Product p ON p.id_product = od.id_product
JOIN Orders  o ON o.id_order   = od.id_order
WHERE o.status_order = 'Selesai'
GROUP BY p.id_product, p.nama_produk
ORDER BY total_terjual DESC;
GO


SELECT TOP 5
    c.id_customer,
    c.nama,
    c.email,
    COUNT(DISTINCT o.id_order)  AS total_order,
    SUM(o.subtotal)             AS total_belanja
FROM Customer c
JOIN Orders o ON o.id_customer = c.id_customer
WHERE o.status_order = 'Selesai'
GROUP BY c.id_customer, c.nama, c.email
ORDER BY total_belanja DESC;
GO


SELECT TOP 3
    p.id_product,
    p.nama_produk,
    COUNT(*)    AS frekuensi_beli_bareng
FROM OrderDetail od1
JOIN OrderDetail od2
    ON  od1.id_order    = od2.id_order
    AND od2.id_product != 'P001'
JOIN Product p ON p.id_product = od2.id_product
WHERE od1.id_product = 'P001'
GROUP BY p.id_product, p.nama_produk
ORDER BY frekuensi_beli_bareng DESC;
GO


CREATE VIEW vw_RingkasanOrder AS
SELECT
    c.id_customer,
    c.nama,
    c.email,
    COUNT(DISTINCT o.id_order)  AS total_order,
    SUM(o.subtotal)             AS total_belanja,
    MAX(o.tanggal_order)        AS terakhir_belanja
FROM Customer c
LEFT JOIN Orders o ON o.id_customer = c.id_customer
GROUP BY c.id_customer, c.nama, c.email;
GO

-- VIEW 2: Detail order lengkap beserta produk
CREATE VIEW vw_DetailOrder AS
SELECT
    o.id_order,
    o.tanggal_order,
    o.status_order,
    c.nama              AS nama_customer,
    c.email,
    p.nama_produk,
    od.jumlah,
    od.harga,
    od.subtotal         AS subtotal_item,
    o.subtotal          AS subtotal_order
FROM Orders o
JOIN Customer    c  ON c.id_customer = o.id_customer
JOIN OrderDetail od ON od.id_order   = o.id_order
JOIN Product     p  ON p.id_product  = od.id_product;
GO


CREATE PROCEDURE sp_BuatOrder
    @id_order       VARCHAR(50),
    @id_customer    VARCHAR(50),
    @subtotal       DECIMAL(12,2),
    @status         VARCHAR(100) = 'Menunggu Konfirmasi'
AS
BEGIN
    BEGIN TRANSACTION
    BEGIN TRY
        INSERT INTO Orders (id_order, tanggal_order, subtotal, status_order, id_customer)
        VALUES (@id_order, GETDATE(), @subtotal, @status, @id_customer);

        COMMIT TRANSACTION;
        PRINT 'Order berhasil dibuat: ' + @id_order;
    END TRY
    BEGIN CATCH
        ROLLBACK TRAN;
        PRINT 'Error: ' + ERROR_MESSAGE();
    END CATCH
END;
GO

CREATE FUNCTION fn_TotalBelanjaCustomer(@id_customer VARCHAR(50))
RETURNS DECIMAL(12,2)
AS
BEGIN
    DECLARE @total DECIMAL(12,2);
    SELECT @total = SUM(subtotal)
    FROM Orders
    WHERE id_customer = @id_customer
    AND   status_order = 'Selesai';
    RETURN ISNULL(@total, 0);
END;
GO
