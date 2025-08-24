package com.hn.tgu.hospital.elasticsearch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorElasticsearchRepository extends ElasticsearchRepository<DoctorElasticsearch, String> {
    
    // Búsquedas básicas
    List<DoctorElasticsearch> findByNameContainingIgnoreCase(String name);
    List<DoctorElasticsearch> findBySpecialty(String specialty);
    List<DoctorElasticsearch> findByHospital(String hospital);
    List<DoctorElasticsearch> findByAvailable(boolean available);
    List<DoctorElasticsearch> findByTagsIn(List<String> tags);
    
    // Búsquedas con rangos
    List<DoctorElasticsearch> findByExperienceYearsBetween(int minYears, int maxYears);
    List<DoctorElasticsearch> findByRatingBetween(double minRating, double maxRating);
    
    // Búsquedas combinadas
    List<DoctorElasticsearch> findBySpecialtyAndAvailable(String specialty, boolean available);
    List<DoctorElasticsearch> findByHospitalAndAvailable(String hospital, boolean available);
    
    // Búsqueda por texto completo
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^3\", \"specialty^2.5\", \"description^1.5\", \"searchText^1\"], \"type\": \"best_fields\", \"tie_breaker\": 0.3}}")
    Page<DoctorElasticsearch> searchByText(String query, Pageable pageable);
    
    // Búsqueda avanzada con facets
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^3\", \"specialty^2.5\", \"description^1.5\"]}}], \"filter\": [{\"term\": {\"specialty\": \"?1\"}}, {\"term\": {\"hospital\": \"?2\"}}, {\"range\": {\"experienceYears\": {\"gte\": ?3, \"lte\": ?4}}}, {\"range\": {\"rating\": {\"gte\": ?5, \"lte\": ?6}}}, {\"term\": {\"available\": ?7}}]}}")
    Page<DoctorElasticsearch> searchAdvanced(String query, String specialty, String hospital, 
                                           int minExperience, int maxExperience, 
                                           double minRating, double maxRating, 
                                           boolean available, Pageable pageable);
    
    // Búsqueda por especialidad con ordenamiento
    List<DoctorElasticsearch> findBySpecialtyOrderByRatingDesc(String specialty);
    
    // Búsqueda por hospital con ordenamiento
    List<DoctorElasticsearch> findByHospitalOrderByRatingDesc(String hospital);
    
    // Búsqueda por tags con ordenamiento
    List<DoctorElasticsearch> findByTagsInOrderByRatingDesc(List<String> tags);
    
    // Búsqueda por disponibilidad con ordenamiento
    List<DoctorElasticsearch> findByAvailableOrderByRatingDesc(boolean available);
    
    // Búsqueda por experiencia con ordenamiento
    List<DoctorElasticsearch> findByExperienceYearsBetweenOrderByRatingDesc(int minYears, int maxYears);
    
    // Búsqueda por rating con ordenamiento
    List<DoctorElasticsearch> findByRatingBetweenOrderByRatingDesc(double minRating, double maxRating);
    
    // Búsqueda por nombre que empiece con (autocompletado)
    List<DoctorElasticsearch> findByNameStartingWithIgnoreCaseOrderByRatingDesc(String name);
    
    // Búsqueda por nombre que contenga (búsqueda parcial)
    List<DoctorElasticsearch> findByNameContainingIgnoreCaseOrderByRatingDesc(String name);
    
    // Búsqueda por descripción
    List<DoctorElasticsearch> findByDescriptionContainingIgnoreCase(String description);
    
    // Búsqueda por días laborales
    List<DoctorElasticsearch> findByDiasLaboralesIn(List<String> diasLaborales);
    
    // Búsqueda por horario de entrada
    List<DoctorElasticsearch> findByHorarioEntrada(String horarioEntrada);
    
    // Búsqueda por horario de salida
    List<DoctorElasticsearch> findByHorarioSalida(String horarioSalida);
    
    // Búsqueda por duración de cita
    List<DoctorElasticsearch> findByDuracionCita(int duracionCita);
    
    // Búsqueda por duración de cita con rango
    List<DoctorElasticsearch> findByDuracionCitaBetween(int minDuracion, int maxDuracion);
    
    // Búsqueda por múltiples especialidades
    List<DoctorElasticsearch> findBySpecialtyIn(List<String> specialties);
    
    // Búsqueda por múltiples hospitales
    List<DoctorElasticsearch> findByHospitalIn(List<String> hospitals);
    
    // Búsqueda por múltiples criterios con ordenamiento
    List<DoctorElasticsearch> findBySpecialtyInAndHospitalInAndAvailableOrderByRatingDesc(
        List<String> specialties, List<String> hospitals, boolean available);
    
    // Búsqueda por texto en múltiples campos con boosting
    @Query("{\"dis_max\": {\"queries\": [{\"match\": {\"name\": {\"query\": \"?0\", \"boost\": 3}}}, {\"match\": {\"specialty\": {\"query\": \"?0\", \"boost\": 2.5}}}, {\"match\": {\"description\": {\"query\": \"?0\", \"boost\": 1.5}}}, {\"match\": {\"searchText\": {\"query\": \"?0\", \"boost\": 1}}}], \"tie_breaker\": 0.3}}")
    Page<DoctorElasticsearch> searchWithBoosting(String query, Pageable pageable);
}
