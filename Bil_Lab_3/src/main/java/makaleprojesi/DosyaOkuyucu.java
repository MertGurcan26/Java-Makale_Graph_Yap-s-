package makaleprojesi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DosyaOkuyucu {

    public List<Makale> jsonDosyasiniOku(String dosyaYolu) {
        List<Makale> makaleler = new ArrayList<>();
        StringBuilder jsonContent = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(dosyaYolu))) {
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line.trim());
            }
        } catch (IOException e) {
            System.out.println("Dosya hatası: " + e.getMessage());
            return makaleler;
        }

        String content = jsonContent.toString();
        if (content.startsWith("[")) content = content.substring(1);
        if (content.endsWith("]")) content = content.substring(0, content.length() - 1);

        String[] objects = content.split("},\\s*\\{");

        for (String objStr : objects) {
            objStr = objStr.replace("{", "").replace("}", "");
            
            String id = extractValue(objStr, "\"id\"");
            String title = extractValue(objStr, "\"title\"");
            String doi = extractValue(objStr, "\"doi\"");
            String venue = extractValue(objStr, "\"venue\"");
            int year = 0;
            try {
                String y = extractValue(objStr, "\"year\"");
                if(!y.isEmpty()) year = Integer.parseInt(y);
            } catch (Exception e) {}

            Makale m = new Makale(id, title, doi, year, venue, 0);

            List<String> authors = extractList(objStr, "\"authors\"");
            for (String a : authors) m.addAuthor(a);

            List<String> refs = extractList(objStr, "\"referenced_works\"");
            for (String r : refs) m.addReferencedWorkId(r);

            makaleler.add(m);
        }
        return makaleler;
    }

    private String extractValue(String source, String key) {
        int keyIndex = source.indexOf(key);
        if (keyIndex == -1) return "";
        int colIndex = source.indexOf(":", keyIndex);
        int start = source.indexOf("\"", colIndex);
        
        if (start == -1 || source.substring(colIndex, colIndex+5).contains(",")) {
            start = colIndex + 1;
            int end = source.indexOf(",", start);
            if(end == -1) end = source.length();
            return source.substring(start, end).trim();
        }
        int end = source.indexOf("\"", start + 1);
        return source.substring(start + 1, end);
    }

    private List<String> extractList(String source, String key) {
        List<String> list = new ArrayList<>();
        int k = source.indexOf(key);
        if (k == -1) return list;
        int s = source.indexOf("[", k);
        int e = source.indexOf("]", s);
        if (s == -1 || e == -1) return list;
        
        String inner = source.substring(s+1, e);
        if(inner.trim().isEmpty()) return list;
        
        String[] parts = inner.split(",");
        for(String p : parts) list.add(p.trim().replace("\"", ""));
        return list;
    }
}