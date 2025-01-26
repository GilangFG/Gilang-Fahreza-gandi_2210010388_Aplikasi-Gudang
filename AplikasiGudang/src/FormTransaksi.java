
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JTable;

public class FormTransaksi extends javax.swing.JFrame {

    /**
     * Creates new form FormTransaksi
     */
    public FormTransaksi() {
        initComponents();
        tampilkanData();
        isiComboBox();

        tblTransaksi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Mendapatkan baris yang diklik
                int selectedRow = tblTransaksi.getSelectedRow();

                // Menampilkan data dari baris yang dipilih ke JTextField
                if (selectedRow != -1) {
                    // Ambil data dari tabel berdasarkan baris yang dipilih
                    String idTransaksi = tblTransaksi.getValueAt(selectedRow, 0).toString();  // ID Transaksi
                    String namaBarang = tblTransaksi.getValueAt(selectedRow, 1).toString();    // Nama Barang
                    String kategori = tblTransaksi.getValueAt(selectedRow, 2).toString();      // Kategori
                    String tanggal = tblTransaksi.getValueAt(selectedRow, 3).toString();      // Tanggal
                    String jumlah = tblTransaksi.getValueAt(selectedRow, 4).toString();       // Jumlah
                    String total = tblTransaksi.getValueAt(selectedRow, 5).toString();        // Total

                    // Isi data di JTextField
                    txtIdTransaksi.setText(idTransaksi);  // Mengisi ID Transaksi
                    txtJumlah.setText(jumlah);            // Mengisi Jumlah
                    txtTotal.setText(total);              // Mengisi Total

                    // Mengatur ComboBox Nama Barang dan Kategori sesuai dengan data yang dipilih
                    cmbNamaBarang.setSelectedItem(namaBarang);  // Mengisi ComboBox Nama Barang
                    cmbKategori.setSelectedItem(kategori);      // Mengisi ComboBox Kategori

                    // Mengatur Tanggal dengan nilai yang dipilih (jika formatnya sesuai)
                    try {
                        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(tanggal);
                        dateChooser.setDate(date);  // Mengatur Tanggal di JDateChooser
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private double getHargaBarang(String namaBarang) {
        double harga = 0.0;
        try {
            // Mengambil koneksi ke database
            Connection con = Koneksi.getKoneksi();
            String sql = "SELECT harga FROM master_barang WHERE nama_barang = ?";

            // Menyiapkan prepared statement
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, namaBarang);

            // Eksekusi query
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                harga = rs.getDouble("harga");  // Ambil harga dari hasil query
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
        return harga;
    }

    private void isiComboBox() {
        try {
            Connection con = Koneksi.getKoneksi();
            String sql = "SELECT nama_barang FROM master_barang";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            // Mengosongkan ComboBox sebelum memasukkan data baru
            cmbNamaBarang.removeAllItems();

            while (rs.next()) {
                cmbNamaBarang.addItem(rs.getString("nama_barang"));
            }

            sql = "SELECT nama_kategori FROM master_kategori";
            pst = con.prepareStatement(sql);
            rs = pst.executeQuery();

            // Mengosongkan ComboBox kategori
            cmbKategori.removeAllItems();

            while (rs.next()) {
                cmbKategori.addItem(rs.getString("nama_kategori"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private void resetForm() {
        // Reset JTextField (ID Transaksi, Jumlah, Total)
        txtIdTransaksi.setText("");  // Mengosongkan ID Transaksi
        txtJumlah.setText("");       // Mengosongkan Jumlah
        txtTotal.setText("");        // Mengosongkan Total

        // Reset ComboBox (Nama Barang, Kategori)
        cmbNamaBarang.setSelectedIndex(0);  // Mengatur ComboBox Nama Barang ke index pertama (Item 1)
        cmbKategori.setSelectedIndex(0);    // Mengatur ComboBox Kategori ke index pertama (Item 1)

        // Reset JDateChooser (Tanggal)
        dateChooser.setDate(null);  // Mengosongkan tanggal di JDateChooser
    }

    public void exportToCSV(JTable table, String fileName) {
        try {
            // Membuat BufferedWriter untuk menulis ke file CSV
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            // Mendapatkan model dari JTable
            DefaultTableModel model = (DefaultTableModel) table.getModel();

            // Menulis nama kolom (header) ke file CSV
            for (int i = 0; i < model.getColumnCount(); i++) {
                writer.write(model.getColumnName(i));
                if (i < model.getColumnCount() - 1) {
                    writer.write(",");
                }
            }
            writer.newLine();

            // Menulis data di setiap baris ke file CSV
            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    writer.write(model.getValueAt(i, j).toString());
                    if (j < model.getColumnCount() - 1) {
                        writer.write(",");
                    }
                }
                writer.newLine();
            }

            // Menutup writer setelah selesai menulis
            writer.close();

            // Memberikan pesan sukses
            JOptionPane.showMessageDialog(null, "Data berhasil diekspor ke " + fileName);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat menyimpan file: " + e.getMessage());
        }
    }

    private void tampilkanData() {
        try {
            // Koneksi ke database
            Connection con = Koneksi.getKoneksi();

            // Query untuk mengambil data dari tabel transaksi
            String sql = "SELECT t.id_transaksi, t.id_barang, t.id_kategori, t.tanggal, t.jumlah, t.total, b.nama_barang, k.nama_kategori "
                    + "FROM transaksi t "
                    + "JOIN master_barang b ON t.id_barang = b.id_barang "
                    + "JOIN master_kategori k ON t.id_kategori = k.id_kategori";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            // Model untuk JTable
            DefaultTableModel model = (DefaultTableModel) tblTransaksi.getModel();
            model.setRowCount(0);  // Menghapus data sebelumnya

            // Menambahkan data ke JTable
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_transaksi"),
                    rs.getString("nama_barang"), // Menampilkan Nama Barang
                    rs.getString("nama_kategori"), // Menampilkan Nama Kategori
                    rs.getDate("tanggal"),
                    rs.getInt("jumlah"),
                    rs.getDouble("total")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cmbNamaBarang = new javax.swing.JComboBox<>();
        cmbKategori = new javax.swing.JComboBox<>();
        txtJumlah = new javax.swing.JTextField();
        txtTotal = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblTransaksi = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        btnTambahTransaksi = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        btnHapusTransaksi = new javax.swing.JButton();
        btnEditTransaksi = new javax.swing.JButton();
        txtIdTransaksi = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jDateChooser = new com.toedter.calendar.JDateChooser();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel1.setText("Transaksi");

        jLabel2.setText("Nama Barang");

        jLabel3.setText("Kategori");

        jLabel4.setText("Jumlah");

        jLabel5.setText("Total");

        jLabel6.setText("Tanggal");

        cmbNamaBarang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih Barang" }));

        cmbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        txtJumlah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtJumlahKeyReleased(evt);
            }
        });

        tblTransaksi.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID Barang", "Nama Barang", "Kategori", "Tanggal", "Jumlah", "Total"
            }
        ));
        jScrollPane1.setViewportView(tblTransaksi);

        jButton1.setText("Kembali");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        btnTambahTransaksi.setText("Tambah");
        btnTambahTransaksi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahTransaksiActionPerformed(evt);
            }
        });

        jButton3.setText("Report");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Reset");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        btnHapusTransaksi.setText("Hapus");
        btnHapusTransaksi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusTransaksiActionPerformed(evt);
            }
        });

        btnEditTransaksi.setText("Edit");
        btnEditTransaksi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditTransaksiActionPerformed(evt);
            }
        });

        txtIdTransaksi.setEditable(false);

        jLabel7.setText("ID Transaksi");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(425, 425, 425)
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addGap(142, 142, 142)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbNamaBarang, 0, 206, Short.MAX_VALUE)
                            .addComponent(cmbKategori, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtJumlah)
                            .addComponent(txtTotal)
                            .addComponent(txtIdTransaksi)
                            .addComponent(jDateChooser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnTambahTransaksi)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnEditTransaksi)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnHapusTransaksi)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 504, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtIdTransaksi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addGap(26, 26, 26)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(cmbNamaBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(24, 24, 24)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(cmbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(20, 20, 20)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtJumlah, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(20, 20, 20)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton1)
                        .addComponent(btnTambahTransaksi)
                        .addComponent(jButton3)
                        .addComponent(jButton4)
                        .addComponent(btnHapusTransaksi)
                        .addComponent(btnEditTransaksi))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel6)
                        .addComponent(jDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(26, 26, 26))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(163, 163, 163))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnTambahTransaksiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahTransaksiActionPerformed
        try {
            // Mendapatkan koneksi ke database
            Connection con = Koneksi.getKoneksi();
            String sql = "INSERT INTO transaksi (id_barang, id_kategori, tanggal, jumlah, total) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);

            // Mengambil nama barang yang dipilih dari combobox
            String namaBarang = cmbNamaBarang.getSelectedItem().toString();
            String kategori = cmbKategori.getSelectedItem().toString();

            // Query untuk mendapatkan ID Barang berdasarkan nama barang yang dipilih
            String sqlBarang = "SELECT id_barang FROM master_barang WHERE nama_barang = ?";
            PreparedStatement pstBarang = con.prepareStatement(sqlBarang);
            pstBarang.setString(1, namaBarang);
            ResultSet rsBarang = pstBarang.executeQuery();
            int idBarang = 0;
            if (rsBarang.next()) {
                idBarang = rsBarang.getInt("id_barang");
            }

            // Query untuk mendapatkan ID Kategori berdasarkan nama kategori yang dipilih
            String sqlKategori = "SELECT id_kategori FROM master_kategori WHERE nama_kategori = ?";
            PreparedStatement pstKategori = con.prepareStatement(sqlKategori);
            pstKategori.setString(1, kategori);
            ResultSet rsKategori = pstKategori.executeQuery();
            int idKategori = 0;
            if (rsKategori.next()) {
                idKategori = rsKategori.getInt("id_kategori");
            }

            // Mengambil tanggal, jumlah, dan total dari input
            java.sql.Date tanggal = new java.sql.Date(dateChooser.getDate().getTime());
            int jumlah = Integer.parseInt(txtJumlah.getText());
            double total = Double.parseDouble(txtTotal.getText());

            // Menyiapkan statement untuk memasukkan data transaksi
            pst.setInt(1, idBarang);
            pst.setInt(2, idKategori);
            pst.setDate(3, tanggal);
            pst.setInt(4, jumlah);
            pst.setDouble(5, total);

            // Menjalankan query untuk menambahkan data
            pst.executeUpdate();
            JOptionPane.showMessageDialog(null, "Transaksi Berhasil Ditambahkan!");

            // Memanggil fungsi untuk menampilkan data terbaru
            tampilkanData();
            resetForm();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }


    }//GEN-LAST:event_btnTambahTransaksiActionPerformed

    private void btnEditTransaksiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditTransaksiActionPerformed
        try {
            // Mengambil ID Transaksi dari input
            int idTransaksi = Integer.parseInt(txtIdTransaksi.getText());

            // Mengambil nama barang dan kategori yang dipilih dari combobox
            String namaBarang = cmbNamaBarang.getSelectedItem().toString();
            String kategori = cmbKategori.getSelectedItem().toString();

            // Query untuk mendapatkan ID Barang berdasarkan nama barang yang dipilih
            Connection con = Koneksi.getKoneksi();
            String sqlBarang = "SELECT id_barang FROM master_barang WHERE nama_barang = ?";
            PreparedStatement pstBarang = con.prepareStatement(sqlBarang);
            pstBarang.setString(1, namaBarang);
            ResultSet rsBarang = pstBarang.executeQuery();
            int idBarang = 0;
            if (rsBarang.next()) {
                idBarang = rsBarang.getInt("id_barang");
            }

            // Query untuk mendapatkan ID Kategori berdasarkan nama kategori yang dipilih
            String sqlKategori = "SELECT id_kategori FROM master_kategori WHERE nama_kategori = ?";
            PreparedStatement pstKategori = con.prepareStatement(sqlKategori);
            pstKategori.setString(1, kategori);
            ResultSet rsKategori = pstKategori.executeQuery();
            int idKategori = 0;
            if (rsKategori.next()) {
                idKategori = rsKategori.getInt("id_kategori");
            }

            // Mengambil tanggal, jumlah, dan total dari input
            java.sql.Date tanggal = new java.sql.Date(dateChooser.getDate().getTime());
            int jumlah = Integer.parseInt(txtJumlah.getText());
            double total = Double.parseDouble(txtTotal.getText());

            // SQL untuk update transaksi
            String sqlUpdate = "UPDATE transaksi SET id_barang = ?, id_kategori = ?, tanggal = ?, jumlah = ?, total = ? WHERE id_transaksi = ?";
            PreparedStatement pstUpdate = con.prepareStatement(sqlUpdate);

            // Menyiapkan parameter untuk update
            pstUpdate.setInt(1, idBarang);
            pstUpdate.setInt(2, idKategori);
            pstUpdate.setDate(3, tanggal);
            pstUpdate.setInt(4, jumlah);
            pstUpdate.setDouble(5, total);
            pstUpdate.setInt(6, idTransaksi);

            // Menjalankan query update
            pstUpdate.executeUpdate();
            JOptionPane.showMessageDialog(null, "Transaksi Berhasil Diubah!");

            // Memanggil fungsi untuk menampilkan data terbaru
            tampilkanData();
            resetForm();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

    }//GEN-LAST:event_btnEditTransaksiActionPerformed

    private void btnHapusTransaksiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusTransaksiActionPerformed
        try {
            int idTransaksi = Integer.parseInt(txtIdTransaksi.getText());  // Ambil ID Transaksi dari input

            Connection con = Koneksi.getKoneksi();
            String sql = "DELETE FROM transaksi WHERE id_transaksi = ?";
            PreparedStatement pst = con.prepareStatement(sql);

            pst.setInt(1, idTransaksi);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(null, "Transaksi Berhasil Dihapus!");

            tampilkanData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }//GEN-LAST:event_btnHapusTransaksiActionPerformed

    private void txtJumlahKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtJumlahKeyReleased
        // Mendapatkan harga barang berdasarkan pilihan di ComboBox
        String selectedItem = (String) cmbNamaBarang.getSelectedItem();
        double harga = getHargaBarang(selectedItem);  // Ambil harga berdasarkan nama barang
        try {
            // Ambil nilai jumlah dari JTextField
            int jumlah = Integer.parseInt(txtJumlah.getText());

            // Hitung total
            double total = harga * jumlah;

            // Set nilai total ke JTextField Total
            txtTotal.setText(String.valueOf(total));
        } catch (NumberFormatException e) {
            // Jika nilai jumlah tidak valid
            txtTotal.setText("0");
        }
    }//GEN-LAST:event_txtJumlahKeyReleased

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        resetForm();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // Menyembunyikan form transaksi
        this.setVisible(false);

        // Menampilkan form utama
        FormUtama formUtama = new FormUtama();  // Pastikan nama class sesuai dengan form utama Anda
        formUtama.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // Menyimpan data dari JTable ke file CSV
        String fileName = "report_transaksi.csv";  // Ganti nama file sesuai dengan form
        exportToCSV(tblTransaksi, fileName);  // Menyimpan data dari JTable tblTransaksi ke CSV
    }//GEN-LAST:event_jButton3ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FormTransaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FormTransaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FormTransaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FormTransaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FormTransaksi().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEditTransaksi;
    private javax.swing.JButton btnHapusTransaksi;
    private javax.swing.JButton btnTambahTransaksi;
    private javax.swing.JComboBox<String> cmbKategori;
    private javax.swing.JComboBox<String> cmbNamaBarang;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private com.toedter.calendar.JDateChooser jDateChooser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblTransaksi;
    private javax.swing.JTextField txtIdTransaksi;
    private javax.swing.JTextField txtJumlah;
    private javax.swing.JTextField txtTotal;
    // End of variables declaration//GEN-END:variables

    private void exportToCSV() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
