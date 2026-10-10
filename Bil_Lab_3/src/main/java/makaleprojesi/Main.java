package makaleprojesi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;

public class Main {
    
    static ArrayList<Makale> tumMakaleler = new ArrayList<>();
    static Map<String, Makale> makaleHaritasi = new HashMap<>();
    
    public static void main(String[] args) {
        System.out.println("=== OTOMATİK BAŞLATILIYOR ===");
        
        // 1. DOSYAYI OKU
        DosyaOkuyucu okuyucu = new DosyaOkuyucu();
        // Dosya ismini kontrol et, 'makaleler.json' mu 'veri.json' mu?
        List<Makale> okunan = okuyucu.jsonDosyasiniOku("makaleler.json"); 
        
        if (okunan.isEmpty()) {
            System.out.println("!!! HATA: Dosya bulunamadı veya içi boş! !!!");
            System.out.println("Lütfen 'makaleler.json' dosyasını proje klasörüne (src'nin yanına) koydugundan emin ol.");
            return;
        }
        
        tumMakaleler.addAll(okunan);
        System.out.println(">> " + tumMakaleler.size() + " makale başarıyla yüklendi.");

        // 2. BAĞLANTILARI KUR
        for (Makale m : tumMakaleler) makaleHaritasi.put(m.getId(), m);
        for (Makale m : tumMakaleler) {
            for (String refId : m.getReferencedWorkIds()) {
                Makale hedef = makaleHaritasi.get(refId);
                if (hedef != null) m.addCitation(hedef);
            }
        }

        // 3. SIRALI BAĞLANTI (YEŞİL KENAR)
        Collections.sort(tumMakaleler);
        for (int i = 0; i < tumMakaleler.size() - 1; i++) {
            tumMakaleler.get(i).setNext(tumMakaleler.get(i+1));
        }

        // 4. OTOMATİK GÖRSELLEŞTİRME (İLK MAKALEYİ AÇ)
        Makale ilkMakale = tumMakaleler.get(0); // Listenin başındakini al
        System.out.println(">> Görselleştirme başlatılıyor: " + ilkMakale.getId());
        
        SwingUtilities.invokeLater(() -> {
            new GorselArayuz(tumMakaleler, ilkMakale);
        });
    }
}