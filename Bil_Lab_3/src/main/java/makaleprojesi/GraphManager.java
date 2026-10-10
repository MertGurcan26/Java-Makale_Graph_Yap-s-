package makaleprojesi;

import java.util.*;

public class GraphManager {

    // 1. H-CORE LİSTESİ ALMA
    public List<Makale> getHCoreList(Makale makale) {
        List<Makale> hCore = new ArrayList<>();
        ArrayList<Makale> atifYapanlar = makale.getAlintilar(); 
        
        if (atifYapanlar == null || atifYapanlar.isEmpty()) return hCore;

        List<Makale> sortedList = new ArrayList<>(atifYapanlar);
        Collections.sort(sortedList, (m1, m2) -> Integer.compare(m2.getAlintilar().size(), m1.getAlintilar().size()));

        for (int i = 0; i < sortedList.size(); i++) {
            if (sortedList.get(i).getAlintilar().size() >= i + 1) hCore.add(sortedList.get(i));
            else break; 
        }
        return hCore;
    }

    public int calculateHIndex(Makale makale) { return getHCoreList(makale).size(); }
    
    public double calculateHMedian(Makale makale) {
        List<Makale> hCore = getHCoreList(makale);
        if (hCore.isEmpty()) return 0;
        List<Integer> counts = new ArrayList<>();
        for (Makale m : hCore) counts.add(m.getAlintilar().size());
        Collections.sort(counts);
        int size = counts.size();
        if (size % 2 == 1) return counts.get(size / 2);
        else return (counts.get((size / 2) - 1) + counts.get(size / 2)) / 2.0;
    }

    // BETWEENNESS CENTRALITY
    public Map<Makale, Double> calculateBetweenness(List<Makale> tumMakaleler) {
        Map<Makale, Double> scores = new HashMap<>();
        for (Makale m : tumMakaleler) scores.put(m, 0.0);

        for (Makale s : tumMakaleler) {
            Stack<Makale> S = new Stack<>();
            Map<Makale, List<Makale>> P = new HashMap<>();
            for (Makale m : tumMakaleler) P.put(m, new ArrayList<>());
            Map<Makale, Integer> sigma = new HashMap<>();
            Map<Makale, Integer> d = new HashMap<>();
            
            for (Makale m : tumMakaleler) { sigma.put(m, 0); d.put(m, -1); }
            sigma.put(s, 1); d.put(s, 0);
            Queue<Makale> Q = new LinkedList<>(); Q.add(s);

            while (!Q.isEmpty()) {
                Makale v = Q.poll();
                S.push(v);
                Set<Makale> komsular = new HashSet<>();
                komsular.addAll(v.getReferanslar());
                komsular.addAll(v.getAlintilar()); // Yönsüz kabul ediliyor

                for (Makale w : komsular) {
                    if(!tumMakaleler.contains(w)) continue; // Sadece listedekiler
                    if (d.get(w) < 0) {
                        Q.add(w);
                        d.put(w, d.get(v) + 1);
                    }
                    if (d.get(w) == d.get(v) + 1) {
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        P.get(w).add(v);
                    }
                }
            }
            Map<Makale, Double> delta = new HashMap<>();
            for (Makale m : tumMakaleler) delta.put(m, 0.0);
            while (!S.isEmpty()) {
                Makale w = S.pop();
                for (Makale v : P.get(w)) {
                    double c = ((double) sigma.get(v) / sigma.get(w)) * (1.0 + delta.get(w));
                    delta.put(v, delta.get(v) + c);
                }
                if (w != s) scores.put(w, scores.get(w) + delta.get(w));
            }
        }
        for (Makale m : scores.keySet()) scores.put(m, scores.get(m) / 2.0);
        return scores;
    }

    // K-CORE
    public List<Makale> runKCore(List<Makale> tumMakaleler, int k) {
        Set<Makale> activeNodes = new HashSet<>(tumMakaleler);
        boolean changed = true;
        while (changed) {
            changed = false;
            Set<Makale> toRemove = new HashSet<>();
            for (Makale m : activeNodes) {
                int degree = 0;
                Set<Makale> neighbors = new HashSet<>();
                neighbors.addAll(m.getReferanslar());
                neighbors.addAll(m.getAlintilar());
                for (Makale n : neighbors) if (activeNodes.contains(n)) degree++;
                if (degree < k) { toRemove.add(m); changed = true; }
            }
            activeNodes.removeAll(toRemove);
        }
        return new ArrayList<>(activeNodes);
    }
}