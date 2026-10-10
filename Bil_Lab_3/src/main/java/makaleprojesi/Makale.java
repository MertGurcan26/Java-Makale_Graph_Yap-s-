package makaleprojesi;

import java.util.ArrayList;
import java.util.List;

public class Makale implements Comparable<Makale> {
    
    private String id;
    private String title;
    private String doi;
    private int year;
    private String venue;
    private int referenceCount;
    
    private List<String> authors;
    private List<String> keywords;
    private List<String> referencedWorkIds; 
    
    private ArrayList<Makale> referanslar; // Ben kime atıf yaptım
    private ArrayList<Makale> alintilar;   // Kim bana atıf yaptı
    
    private Makale next; // Yeşil kenar için

    public Makale(String id, String title, String doi, int year, String venue, int referenceCount) {
        this.id = id;
        this.title = title;
        this.doi = doi;
        this.year = year;
        this.venue = venue;
        this.referenceCount = referenceCount;
        
        this.authors = new ArrayList<>();
        this.keywords = new ArrayList<>();
        this.referencedWorkIds = new ArrayList<>();
        this.referanslar = new ArrayList<>();
        this.alintilar = new ArrayList<>();
        this.next = null;
    }

    public void addAuthor(String author) { this.authors.add(author); }
    public void addReferencedWorkId(String targetId) { this.referencedWorkIds.add(targetId); }

    public void addCitation(Makale hedefMakale) {
        if (!this.referanslar.contains(hedefMakale)) {
            this.referanslar.add(hedefMakale);
            hedefMakale.getAlintilar().add(this);
        }
    }

    @Override
    public int compareTo(Makale other) {
        return this.id.compareTo(other.id);
    }

    // Getterlar
    public String getId() { return id; }
    public String getTitle() { return title; }
    public int getYear() { return year; }
    public List<String> getAuthors() { return authors; }
    public List<String> getReferencedWorkIds() { return referencedWorkIds; }
    public ArrayList<Makale> getReferanslar() { return referanslar; }
    public ArrayList<Makale> getAlintilar() { return alintilar; }
    
    public Makale getNext() { return next; }
    public void setNext(Makale next) { this.next = next; }
    
    @Override
    public String toString() { return title; }
}