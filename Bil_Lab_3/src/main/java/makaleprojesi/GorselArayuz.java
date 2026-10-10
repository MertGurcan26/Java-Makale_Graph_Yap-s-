package makaleprojesi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GorselArayuz extends JFrame {

    // Veri Yapıları
    private ArrayList<Makale> tumVeriTabani;
    private List<Makale> cizilecekMakaleler;
    private Set<Makale> yeniEklenenler;
    private Set<Makale> kCoreSonucu;
    private Makale merkezMakale;
    
    // Görsel Hesaplama
    private Map<Makale, Point> koordinatlar;
    private Map<Makale, Double> betweennessScores;
    
    // Swing Bileşenleri (Arayüz)
    private CizimPaneli cizimPaneli; // Grafı çizen özel panel
    private JTextArea bilgiPaneli;   // Sağdaki yazı alanı

    public GorselArayuz(ArrayList<Makale> tumVeri, Makale baslangicNode) {
        super("Proje 3: Gelişmiş Graf Analiz Sistemi");
        this.tumVeriTabani = tumVeri;
        this.cizilecekMakaleler = new ArrayList<>();
        this.yeniEklenenler = new HashSet<>();
        this.kCoreSonucu = null;
        this.koordinatlar = new HashMap<>();
        
        // Pencere Ayarları
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. SOL TARAFA ÇİZİM PANELİNİ KUR
        cizimPaneli = new CizimPaneli();
        cizimPaneli.setBackground(Color.WHITE);

        // 2. SAĞ TARAFA ANALİZ PANELİNİ KUR
        bilgiPaneli = new JTextArea();
        bilgiPaneli.setEditable(false);
        bilgiPaneli.setFont(new Font("Consolas", Font.PLAIN, 14));
        bilgiPaneli.setBackground(new Color(240, 240, 240));
        bilgiPaneli.setMargin(new Insets(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(bilgiPaneli);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📊 Analiz Sonuçları"));
        
        // 3. EKRANI İKİYE BÖL (SPLIT PANE - Kaymayı Önler)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cizimPaneli, scrollPane);
        splitPane.setDividerLocation(1000); // Başlangıçta çizgi nerede dursun
        splitPane.setResizeWeight(0.8);     // Pencere büyürse graf büyüsün
        add(splitPane, BorderLayout.CENTER);

        // 4. ALT TARAFA BUTONLARI KOY
        JPanel buttonPanel = new JPanel();
        JButton btnTumu = new JButton("TÜMÜNÜ GÖSTER");
        JButton btnSifirla = new JButton("Sıfırla");
        JButton btnBC = new JButton("Betweenness");
        JButton btnKCore = new JButton("K-Core");

        buttonPanel.add(btnTumu);
        buttonPanel.add(btnSifirla);
        buttonPanel.add(btnBC);
        buttonPanel.add(btnKCore);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- İŞLEVLER ---
        grafiGenislet(baslangicNode); // İlk açılış

        // Tıklama Olayı (Mouse Click)
        cizimPaneli.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Makale tiklanan = tiklamaKontrol(e.getX(), e.getY());
                if (tiklanan != null) {
                    grafiGenislet(tiklanan);
                }
            }
        });

        // Buton Olayları
        btnTumu.addActionListener(e -> {
            cizilecekMakaleler = new ArrayList<>(tumVeriTabani);
            yeniEklenenler.clear();
            kCoreSonucu = null;
            betweennessScores = null;
            merkezMakale = null;
            paneliGuncelle("Tüm veritabanı görüntülendi.\nToplam: " + cizilecekMakaleler.size() + " makale.");
            cizimPaneli.repaint();
        });

        btnSifirla.addActionListener(e -> {
            cizilecekMakaleler.clear();
            grafiGenislet(baslangicNode);
        });

        btnBC.addActionListener(e -> {
            GraphManager gm = new GraphManager();
            betweennessScores = gm.calculateBetweenness(cizilecekMakaleler);
            paneliGuncelle("Betweenness Centrality (BC) hesaplandı.\nDetaylar aşağıda listelenmiştir...");
            cizimPaneli.repaint();
        });

        btnKCore.addActionListener(e -> {
            String val = JOptionPane.showInputDialog(this, "k değerini giriniz (Örn: 2):");
            try {
                int k = Integer.parseInt(val);
                GraphManager gm = new GraphManager();
                List<Makale> coreList = gm.runKCore(cizilecekMakaleler, k);
                kCoreSonucu = new HashSet<>(coreList);
                cizilecekMakaleler = coreList;
                merkezMakale = null; // Renk karışmasın diye merkezi iptal et
                
                paneliGuncelle("K-Core (" + k + ") Analizi Tamamlandı.\nElenen Düğüm Sayısı: " + (tumVeriTabani.size() - coreList.size()));
                cizimPaneli.repaint();
            } catch (Exception ex) {}
        });

        setVisible(true);
    }

    // --- MANTIK METOTLARI ---

    private void grafiGenislet(Makale merkez) {
        this.merkezMakale = merkez;
        yeniEklenenler.clear();
        kCoreSonucu = null;
        
        if (!cizilecekMakaleler.contains(merkez)) {
            cizilecekMakaleler.add(merkez);
            yeniEklenenler.add(merkez);
        }

        GraphManager gm = new GraphManager();
        List<Makale> hCore = gm.getHCoreList(merkez);
        
        // Eğer H-Core boşsa en azından komşuları getir (Grafik boş kalmasın)
        if (hCore.isEmpty()) {
            hCore.addAll(merkez.getAlintilar());
            hCore.addAll(merkez.getReferanslar());
        }

        for (Makale m : hCore) {
            if (!cizilecekMakaleler.contains(m)) {
                cizilecekMakaleler.add(m);
                yeniEklenenler.add(m);
            }
        }
        
        // Yan paneli güncelle
        StringBuilder sb = new StringBuilder();
        sb.append("📍 SEÇİLEN MAKALE:\n");
        sb.append("ID: ").append(merkez.getId()).append("\n");
        sb.append("Başlık: ").append(merkez.getTitle()).append("\n");
        sb.append("Yıl: ").append(merkez.getYear()).append("\n");
        sb.append("---------------------------\n");
        sb.append("📊 İSTATİSTİKLER:\n");
        sb.append("Atıf Alan (Size Gelen): ").append(merkez.getAlintilar().size()).append("\n");
        sb.append("Referans Verilen (Sizden Giden): ").append(merkez.getReferanslar().size()).append("\n");
        sb.append("H-Index: ").append(gm.calculateHIndex(merkez)).append("\n");
        sb.append("H-Median: ").append(gm.calculateHMedian(merkez)).append("\n");
        sb.append("---------------------------\n");
        sb.append("Genişleme Sonucu: ").append(yeniEklenenler.size()).append(" yeni düğüm eklendi.\n");
        
        paneliGuncelle(sb.toString());
        cizimPaneli.repaint();
    }

    private void paneliGuncelle(String metin) {
        bilgiPaneli.setText(metin);
        // Eğer Betweenness varsa onu da ekle
        if (betweennessScores != null) {
            bilgiPaneli.append("\n\n🏆 BETWEENNESS SKORLARI:\n");
            for (Makale m : cizilecekMakaleler) {
                bilgiPaneli.append(kisaIDVer(m.getId()) + " -> " + String.format("%.2f", betweennessScores.get(m)) + "\n");
            }
        }
    }

    private Makale tiklamaKontrol(int x, int y) {
        int cap = 60; // Top çapı
        for (Makale m : cizilecekMakaleler) {
            Point p = koordinatlar.get(m);
            if (p != null && p.distance(x, y) < cap / 2) return m;
        }
        return null;
    }
    
    // Uzun ID'leri kısaltan yardımcı metot (W211753... -> W211..53)
    private String kisaIDVer(String longID) {
        if (longID == null) return "Unknown";
        // Eğer URL ise sonunu al (openalex.org/W12345 -> W12345)
        String shortID = longID;
        if (shortID.contains("/")) {
            shortID = shortID.substring(shortID.lastIndexOf("/") + 1);
        }
        // Hala çok uzunsa ilk 8 karakteri al
        if (shortID.length() > 8) {
            return shortID.substring(0, 8) + "..";
        }
        return shortID;
    }

    // --- İÇ SINIF: ÖZEL ÇİZİM PANELİ ---
    private class CizimPaneli extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (cizilecekMakaleler.isEmpty()) {
                g2.drawString("Gösterilecek makale yok...", 50, 50);
                return;
            }

            // Koordinatları Hesapla (Merkezden Daire Şeklinde)
            koordinatlar.clear();
            int w = getWidth();
            int h = getHeight();
            int r = Math.min(w, h) / 3; // Yarıçap
            double angleStep = 360.0 / cizilecekMakaleler.size();

            for (int i = 0; i < cizilecekMakaleler.size(); i++) {
                double rad = Math.toRadians(i * angleStep - 90);
                int x = (int) (w / 2 + Math.cos(rad) * r);
                int y = (int) (h / 2 + Math.sin(rad) * r);
                koordinatlar.put(cizilecekMakaleler.get(i), new Point(x, y));
            }

            // 1. ADIM: YEŞİL KENARLAR (ID Sırası - Kesikli Çizgi)
            Stroke normalStroke = g2.getStroke();
            Stroke dashStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
            g2.setStroke(dashStroke);
            g2.setColor(new Color(0, 150, 0)); // Koyu Yeşil

            for (Makale m : cizilecekMakaleler) {
                if (m.getNext() != null && cizilecekMakaleler.contains(m.getNext())) {
                    Point p1 = koordinatlar.get(m);
                    Point p2 = koordinatlar.get(m.getNext());
                    if (p1 != null && p2 != null) g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
            g2.setStroke(normalStroke);

            // 2. ADIM: SİYAH OKLAR (Atıflar)
            g2.setColor(Color.GRAY);
            for (Makale m : cizilecekMakaleler) {
                Point p1 = koordinatlar.get(m);
                for (Makale ref : m.getReferanslar()) {
                    if (cizilecekMakaleler.contains(ref)) {
                        Point p2 = koordinatlar.get(ref);
                        if (p1 != null && p2 != null) {
                            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                            // Ok ucu (Basit)
                            int mx = (p1.x + p2.x * 4) / 5;
                            int my = (p1.y + p2.y * 4) / 5;
                            g2.fillOval(mx - 3, my - 3, 6, 6);
                        }
                    }
                }
            }

            // 3. ADIM: TOPLAR (Düğümler)
            int cap = 60; // Top boyutu
            for (Makale m : cizilecekMakaleler) {
                Point p = koordinatlar.get(m);
                int x = p.x - cap / 2;
                int y = p.y - cap / 2;

                // Renk Seçimi
                if (kCoreSonucu != null && kCoreSonucu.contains(m)) g2.setColor(new Color(255, 165, 0)); // Turuncu
                else if (m == merkezMakale) g2.setColor(new Color(255, 50, 50)); // Kırmızı (Merkez)
                else if (yeniEklenenler.contains(m)) g2.setColor(new Color(100, 255, 100)); // Yeşil (Yeni)
                else g2.setColor(new Color(220, 220, 220)); // Gri (Eski)

                g2.fillOval(x, y, cap, cap);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(x, y, cap, cap);

                // KISA ID YAZDIRMA
                g2.setStroke(normalStroke);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                String text = kisaIDVer(m.getId());
                int textW = g2.getFontMetrics().stringWidth(text);
                g2.drawString(text, p.x - textW / 2, p.y + 5);
                
                // Betweenness Skoru (Varsa üstüne yaz)
                if(betweennessScores != null) {
                    g2.setColor(new Color(0, 100, 0));
                    String bc = String.format("%.1f", betweennessScores.get(m));
                    g2.drawString(bc, p.x - 10, p.y - cap/2 - 5);
                    g2.setColor(Color.BLACK);
                }
            }
        }
    }
}