package controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import data.ProdukRepository;
import model.Produk;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class KatalogController {

    @FXML
    private TextField namaField;
    @FXML
    private TextField hargaField;
    @FXML
    private ComboBox<String> fotoBox;
    @FXML
    private Label pesanLabel;
    @FXML
    private Label jumlahLabel;
    @FXML
    private TilePane katalogPane;
    private final ProdukRepository repository = new ProdukRepository();
    private Produk produkDiedit = null;

    @FXML
    private void initialize() {
        fotoBox.getItems().add("roti.png");
        fotoBox.getItems().add("susu.png");
        fotoBox.getItems().add("kopi.png");
        fotoBox.setValue("roti.png");
        try {
            showData();
        } catch (SQLException e) {
            pesanLabel.setText("Gagal membaca database. Lihat Output.");
            e.printStackTrace();
        }
    }

    private void showData() throws SQLException {
        ArrayList<Produk> daftar = repository.getAll();
        katalogPane.getChildren().clear();
        for (Produk produk : daftar) {
            VBox kartu = createProductCard(produk);
            katalogPane.getChildren().add(kartu);
        }
        jumlahLabel.setText("Jumlah produk: " + daftar.size());
    }

    private VBox createProductCard(Produk produk) {
        ImageView fotoView = new ImageView();
        fotoView.setFitWidth(170);
        fotoView.setFitHeight(110);
        fotoView.setPreserveRatio(true);
        StackPane bingkai = new StackPane();
        bingkai.setPrefSize(180, 120);
        bingkai.setMinSize(180, 120);
        bingkai.setMaxSize(180, 120);

        URL lokasi = getClass().getResource("/image/" + produk.getFoto());
        if (lokasi != null) {
            Image gambar = new Image(lokasi.toExternalForm(),
                    170, 110, true, true);
            if (!gambar.isError()) {
                fotoView.setImage(gambar);
                bingkai.getChildren().add(fotoView);
            } else {
                bingkai.getChildren().add(new Label("Gambar tidak terbaca"));
            }
        } else {
            bingkai.getChildren().add(new Label("Gambar tidak tersedia"));
        }

        Label namaLabel = new Label(produk.getNama());
        namaLabel.setWrapText(true);
        namaLabel.getStyleClass().add("nama-produk");
        Label hargaLabel = new Label("Rp" + produk.getHarga());
        Label idLabel = new Label("ID: " + produk.getId());
        idLabel.getStyleClass().add("muted");

        VBox kartu = new VBox();
        kartu.setSpacing(8);
        kartu.setMinWidth(220);
        kartu.setPrefWidth(220);
        kartu.setMaxWidth(220);
        kartu.getStyleClass().add("kartu-produk");
        kartu.getChildren().add(bingkai);
        kartu.getChildren().add(namaLabel);
        kartu.getChildren().add(hargaLabel);
        kartu.getChildren().add(idLabel);
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("button-edit");
        editBtn.setOnAction(e -> startEdit(produk));

        Button hapusBtn = new Button("Hapus");
        hapusBtn.getStyleClass().add("button-delete");
        hapusBtn.setOnAction(e -> deleteProduct(produk));

        HBox aksiBox = new HBox(8, editBtn, hapusBtn);
        kartu.getChildren().add(aksiBox);
        return kartu;
    }

    private void startEdit(Produk produk) {
    produkDiedit = produk;
    namaField.setText(produk.getNama());
    hargaField.setText(String.valueOf(produk.getHarga()));
    fotoBox.setValue(produk.getFoto());
    pesanLabel.setText("Mode edit ID " + produk.getId() + ". Ubah data lalu klik Simpan.");
}

private void deleteProduct(Produk produk) {
    try {
        repository.delete(produk.getId());
        // Jika produk yang sedang diedit ternyata dihapus, reset form
        if (produkDiedit != null && produkDiedit.getId() == produk.getId()) {
            produkDiedit = null;
            namaField.clear();
            hargaField.clear();
        }
        showData();
        pesanLabel.setText("Produk \"" + produk.getNama() + "\" berhasil dihapus.");
    } catch (SQLException e) {
        pesanLabel.setText("Gagal menghapus produk. Lihat Output.");
        e.printStackTrace();
    }
}

@FXML
private void saveProduct() {
    Produk produk;
    try {
        String nama = namaField.getText();
        long harga = Long.parseLong(hargaField.getText().trim());
        String foto = fotoBox.getValue();

        if (produkDiedit == null) {
            produk = new Produk(nama, harga, foto);
        } else {
            produk = new Produk(produkDiedit.getId(), nama, harga, foto);
        }
    } catch (NumberFormatException e) {
        pesanLabel.setText("Harga harus angka bulat, contoh 12000.");
        return;
    } catch (IllegalArgumentException e) {
        pesanLabel.setText(e.getMessage());
        return;
    }

    try {
        if (produkDiedit == null) {
            repository.add(produk);
            pesanLabel.setText("Produk berhasil disimpan.");
        } else {
            repository.update(produk);
            pesanLabel.setText("Produk ID " + produk.getId() + " berhasil diperbarui.");
            produkDiedit = null;
        }
    } catch (SQLException e) {
        pesanLabel.setText("Gagal menyimpan ke database. Lihat Output.");
        e.printStackTrace();
        return;
    }

    namaField.clear();
    hargaField.clear();

    try {
        showData();
    } catch (SQLException e) {
        pesanLabel.setText("Data tersimpan, tetapi katalog gagal dimuat ulang.");
        e.printStackTrace();
    }
}
    
}
