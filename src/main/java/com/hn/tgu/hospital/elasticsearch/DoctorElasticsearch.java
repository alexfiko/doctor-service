package com.hn.tgu.hospital.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.List;
import java.util.Map;

@Document(indexName = "doctores")
@Setting(settingPath = "elasticsearch-settings.json")
public class DoctorElasticsearch {
    
    @Id
    private String id;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;
    
    @Field(type = FieldType.Keyword)
    private String specialty;
    
    @Field(type = FieldType.Text)
    private String img;
    
    @Field(type = FieldType.Integer)
    private int experienceYears;
    
    @Field(type = FieldType.Double)
    private double rating;
    
    @Field(type = FieldType.Keyword)
    private String hospital;
    
    @Field(type = FieldType.Boolean)
    private boolean available;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Keyword)
    private List<String> tags;
    
    @Field(type = FieldType.Keyword)
    private List<String> diasLaborales;
    
    @Field(type = FieldType.Keyword)
    private String horarioEntrada;
    
    @Field(type = FieldType.Keyword)
    private String horarioSalida;
    
    @Field(type = FieldType.Integer)
    private int duracionCita;
    
    @Field(type = FieldType.Object)
    private Map<String, List<String>> horariosDisponibles;
    
    // Campo adicional para búsqueda de texto completo
    @Field(type = FieldType.Text, analyzer = "standard")
    private String searchText;
    
    // Constructores
    public DoctorElasticsearch() {}
    
    public DoctorElasticsearch(String id, String name, String specialty, String img, int experienceYears, 
                              double rating, String hospital, boolean available, String description, 
                              List<String> tags, List<String> diasLaborales, String horarioEntrada, 
                              String horarioSalida, int duracionCita, Map<String, List<String>> horariosDisponibles) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.img = img;
        this.experienceYears = experienceYears;
        this.rating = rating;
        this.hospital = hospital;
        this.available = available;
        this.description = description;
        this.tags = tags;
        this.diasLaborales = diasLaborales;
        this.horarioEntrada = horarioEntrada;
        this.horarioSalida = horarioSalida;
        this.duracionCita = duracionCita;
        this.horariosDisponibles = horariosDisponibles;
        this.searchText = name + " " + specialty + " " + description;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        updateSearchText();
    }
    
    public String getSpecialty() {
        return specialty;
    }
    
    public void setSpecialty(String specialty) {
        this.specialty = specialty;
        updateSearchText();
    }
    
    public String getImg() {
        return img;
    }
    
    public void setImg(String img) {
        this.img = img;
    }
    
    public int getExperienceYears() {
        return experienceYears;
    }
    
    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }
    
    public double getRating() {
        return rating;
    }
    
    public void setRating(double rating) {
        this.rating = rating;
    }
    
    public String getHospital() {
        return hospital;
    }
    
    public void setHospital(String hospital) {
        this.hospital = hospital;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        updateSearchText();
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public List<String> getDiasLaborales() {
        return diasLaborales;
    }
    
    public void setDiasLaborales(List<String> diasLaborales) {
        this.diasLaborales = diasLaborales;
    }
    
    public String getHorarioEntrada() {
        return horarioEntrada;
    }
    
    public void setHorarioEntrada(String horarioEntrada) {
        this.horarioEntrada = horarioEntrada;
    }
    
    public String getHorarioSalida() {
        return horarioSalida;
    }
    
    public void setHorarioSalida(String horarioSalida) {
        this.horarioSalida = horarioSalida;
    }
    
    public int getDuracionCita() {
        return duracionCita;
    }
    
    public void setDuracionCita(int duracionCita) {
        this.duracionCita = duracionCita;
    }
    
    public Map<String, List<String>> getHorariosDisponibles() {
        return horariosDisponibles;
    }
    
    public void setHorariosDisponibles(Map<String, List<String>> horariosDisponibles) {
        this.horariosDisponibles = horariosDisponibles;
    }
    
    public String getSearchText() {
        return searchText;
    }
    
    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
    
    private void updateSearchText() {
        this.searchText = name + " " + specialty + " " + description;
    }
}
