package com.hn.tgu.hospital.elasticsearch;

import com.hn.tgu.hospital.entity.Doctor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorElasticsearchService {
    
    @Autowired
    private DoctorElasticsearchRepository doctorElasticsearchRepository;
    
    @Autowired
    private RestHighLevelClient elasticsearchClient;
    
    private static final String INDEX_NAME = "doctores";
    
    /**
     * Búsqueda con facets para especialidades
     */
    public Map<String, Object> searchWithFacets(String query, String specialty, String hospital, 
                                               Integer minExperience, Integer maxExperience, 
                                               Double minRating, Double maxRating, 
                                               Boolean available, List<String> tags, 
                                               int page, int size) {
        
        try {
            // Crear búsqueda principal
            SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            // Construir query principal
            var boolQuery = org.elasticsearch.index.query.QueryBuilders.boolQuery();
            
            // Query de texto libre
            if (query != null && !query.trim().isEmpty()) {
                var multiMatch = org.elasticsearch.index.query.QueryBuilders.multiMatchQuery(query)
                    .field("name", 3.0f)
                    .field("specialty", 2.5f)
                    .field("description", 1.5f)
                    .field("searchText", 1.0f)
                    .type(org.elasticsearch.index.query.MultiMatchQueryBuilder.Type.BEST_FIELDS)
                    .tieBreaker(0.3f);
                boolQuery.must(multiMatch);
            }
            
            // Filtros
            if (specialty != null && !specialty.trim().isEmpty()) {
                boolQuery.filter(org.elasticsearch.index.query.QueryBuilders.termQuery("specialty", specialty));
            }
            
            if (hospital != null && !hospital.trim().isEmpty()) {
                boolQuery.filter(org.elasticsearch.index.query.QueryBuilders.termQuery("hospital", hospital));
            }
            
            if (available != null) {
                boolQuery.filter(org.elasticsearch.index.query.QueryBuilders.termQuery("available", available));
            }
            
            if (minExperience != null || maxExperience != null) {
                var rangeQuery = org.elasticsearch.index.query.QueryBuilders.rangeQuery("experienceYears");
                if (minExperience != null) rangeQuery.gte(minExperience);
                if (maxExperience != null) rangeQuery.lte(maxExperience);
                boolQuery.filter(rangeQuery);
            }
            
            if (minRating != null || maxRating != null) {
                var rangeQuery = org.elasticsearch.index.query.QueryBuilders.rangeQuery("rating");
                if (minRating != null) rangeQuery.gte(minRating);
                if (maxRating != null) rangeQuery.lte(maxRating);
                boolQuery.filter(rangeQuery);
            }
            
            if (tags != null && !tags.isEmpty()) {
                boolQuery.filter(org.elasticsearch.index.query.QueryBuilders.termsQuery("tags", tags));
            }
            
            sourceBuilder.query(boolQuery);
            
            // Configurar paginación
            sourceBuilder.from(page * size).size(size);
            sourceBuilder.sort("_score", org.elasticsearch.search.sort.SortOrder.DESC);
            sourceBuilder.sort("rating", org.elasticsearch.search.sort.SortOrder.DESC);
            
            // Agregar facets/agregaciones
            addFacets(sourceBuilder);
            
            searchRequest.source(sourceBuilder);
            
            // Ejecutar búsqueda
            SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
            
            // Procesar resultados
            Map<String, Object> result = new HashMap<>();
            
            // Resultados principales
            List<DoctorElasticsearch> doctors = Arrays.stream(response.getHits().getHits())
                .map(hit -> doctorElasticsearchRepository.findById(hit.getId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            result.put("doctors", doctors);
            result.put("totalHits", response.getHits().getTotalHits().value);
            result.put("page", page);
            result.put("size", size);
            
            // Procesar facets
            Map<String, Object> facets = processFacets(response);
            result.put("facets", facets);
            
            return result;
            
        } catch (IOException e) {
            throw new RuntimeException("Error en búsqueda con facets", e);
        }
    }
    
    /**
     * Agregar facets a la búsqueda
     */
    private void addFacets(SearchSourceBuilder sourceBuilder) {
        // Facet para especialidades
        TermsAggregationBuilder specialtyAgg = AggregationBuilders.terms("specialties")
            .field("specialty")
            .size(50);
        sourceBuilder.aggregation(specialtyAgg);
        
        // Facet para hospitales
        TermsAggregationBuilder hospitalAgg = AggregationBuilders.terms("hospitals")
            .field("hospital")
            .size(50);
        sourceBuilder.aggregation(hospitalAgg);
        
        // Facet para tags
        TermsAggregationBuilder tagsAgg = AggregationBuilders.terms("tags")
            .field("tags")
            .size(100);
        sourceBuilder.aggregation(tagsAgg);
        
        // Facet para días laborales
        TermsAggregationBuilder diasAgg = AggregationBuilders.terms("diasLaborales")
            .field("diasLaborales")
            .size(20);
        sourceBuilder.aggregation(diasAgg);
        
        // Facet para rangos de experiencia
        sourceBuilder.aggregation(AggregationBuilders.range("experienceRanges")
            .field("experienceYears")
            .addRange("0-5", 0, 5)
            .addRange("6-10", 6, 10)
            .addRange("11-15", 11, 15)
            .addRange("16-20", 16, 20)
            .addRange("21+", 21, 100));
        
        // Facet para rangos de rating
        sourceBuilder.aggregation(AggregationBuilders.range("ratingRanges")
            .field("rating")
            .addRange("1-2", 1.0, 2.0)
            .addRange("2-3", 2.0, 3.0)
            .addRange("3-4", 3.0, 4.0)
            .addRange("4-5", 4.0, 5.0));
        
        // Estadísticas de rating
        sourceBuilder.aggregation(AggregationBuilders.avg("avgRating").field("rating"));
        
        // Estadísticas de experiencia
        sourceBuilder.aggregation(AggregationBuilders.avg("avgExperience").field("experienceYears"));
        
        // Conteo por disponibilidad
        sourceBuilder.aggregation(AggregationBuilders.terms("disponibilidad")
            .field("available")
            .size(2));
    }
    
    /**
     * Procesar facets de la respuesta
     */
    private Map<String, Object> processFacets(SearchResponse response) {
        Map<String, Object> facets = new HashMap<>();
        
        // Especialidades
        Terms specialties = response.getAggregations().get("specialties");
        if (specialties != null) {
            List<Map<String, Object>> specialtyFacets = specialties.getBuckets().stream()
                .map(bucket -> {
                    Map<String, Object> facet = new HashMap<>();
                    facet.put("value", bucket.getKeyAsString());
                    facet.put("count", bucket.getDocCount());
                    return facet;
                })
                .collect(Collectors.toList());
            facets.put("specialties", specialtyFacets);
        }
        
        // Hospitales
        Terms hospitals = response.getAggregations().get("hospitals");
        if (hospitals != null) {
            List<Map<String, Object>> hospitalFacets = hospitals.getBuckets().stream()
                .map(bucket -> {
                    Map<String, Object> facet = new HashMap<>();
                    facet.put("value", bucket.getKeyAsString());
                    facet.put("count", bucket.getDocCount());
                    return facet;
                })
                .collect(Collectors.toList());
            facets.put("hospitals", hospitalFacets);
        }
        
        // Tags
        Terms tags = response.getAggregations().get("tags");
        if (tags != null) {
            List<Map<String, Object>> tagFacets = tags.getBuckets().stream()
                .map(bucket -> {
                    Map<String, Object> facet = new HashMap<>();
                    facet.put("value", bucket.getKeyAsString());
                    facet.put("count", bucket.getDocCount());
                    return facet;
                })
                .collect(Collectors.toList());
            facets.put("tags", tagFacets);
        }
        
        // Días laborales
        Terms diasLaborales = response.getAggregations().get("diasLaborales");
        if (diasLaborales != null) {
            List<Map<String, Object>> diasFacets = diasLaborales.getBuckets().stream()
                .map(bucket -> {
                    Map<String, Object> facet = new HashMap<>();
                    facet.put("value", bucket.getKeyAsString());
                    facet.put("count", bucket.getDocCount());
                    return facet;
                })
                .collect(Collectors.toList());
            facets.put("diasLaborales", diasFacets);
        }
        
        // Rating promedio
        Avg avgRating = response.getAggregations().get("avgRating");
        if (avgRating != null) {
            facets.put("avgRating", avgRating.getValue());
        }
        
        // Experiencia promedio
        Avg avgExperience = response.getAggregations().get("avgExperience");
        if (avgExperience != null) {
            facets.put("avgExperience", avgExperience.getValue());
        }
        
        return facets;
    }
    
    /**
     * Sincronizar entidad JPA con Elasticsearch
     */
    public DoctorElasticsearch syncFromJPA(Doctor doctor) {
        DoctorElasticsearch doctorES = new DoctorElasticsearch(
            doctor.getId(),
            doctor.getName(),
            doctor.getSpecialty(),
            doctor.getImg(),
            doctor.getExperienceYears(),
            doctor.getRating(),
            doctor.getHospital(),
            doctor.isAvailable(),
            doctor.getDescription(),
            doctor.getTags(),
            doctor.getDiasLaborales(),
            doctor.getHorarioEntrada(),
            doctor.getHorarioSalida(),
            doctor.getDuracionCita(),
            doctor.getHorariosDisponibles()
        );
        
        return doctorElasticsearchRepository.save(doctorES);
    }
    
    /**
     * Búsqueda simple por texto
     */
    public Page<DoctorElasticsearch> searchByText(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return doctorElasticsearchRepository.searchByText(query, pageable);
    }
    
    /**
     * Búsqueda avanzada
     */
    public Page<DoctorElasticsearch> searchAdvanced(String query, String specialty, String hospital, 
                                                   int minExperience, int maxExperience, 
                                                   double minRating, double maxRating, 
                                                   boolean available, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return doctorElasticsearchRepository.searchAdvanced(query, specialty, hospital, 
                                                          minExperience, maxExperience, 
                                                          minRating, maxRating, 
                                                          available, pageable);
    }
    
    /**
     * Obtener todas las especialidades disponibles
     */
    public List<String> getAllSpecialties() {
        return doctorElasticsearchRepository.findAll().stream()
            .map(DoctorElasticsearch::getSpecialty)
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener todos los hospitales disponibles
     */
    public List<String> getAllHospitals() {
        return doctorElasticsearchRepository.findAll().stream()
            .map(DoctorElasticsearch::getHospital)
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * Obtener todos los tags disponibles
     */
    public List<String> getAllTags() {
        return doctorElasticsearchRepository.findAll().stream()
            .flatMap(d -> d.getTags() != null ? d.getTags().stream() : Stream.empty())
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * Buscar doctor por ID
     */
    public Optional<DoctorElasticsearch> findById(String id) {
        return doctorElasticsearchRepository.findById(id);
    }
    
    /**
     * Obtener todos los doctores
     */
    public List<DoctorElasticsearch> findAll() {
        List<DoctorElasticsearch> doctors = new ArrayList<>();
        doctorElasticsearchRepository.findAll().forEach(doctors::add);
        return doctors;
    }
    
    /**
     * Buscar por especialidad
     */
    public List<DoctorElasticsearch> findBySpecialty(String specialty) {
        return doctorElasticsearchRepository.findBySpecialty(specialty);
    }
    
    /**
     * Buscar por hospital
     */
    public List<DoctorElasticsearch> findByHospital(String hospital) {
        return doctorElasticsearchRepository.findByHospital(hospital);
    }
    
    /**
     * Buscar por disponibilidad
     */
    public List<DoctorElasticsearch> findByAvailable(boolean available) {
        return doctorElasticsearchRepository.findByAvailable(available);
    }
    
    /**
     * Buscar por tags
     */
    public List<DoctorElasticsearch> findByTagsIn(List<String> tags) {
        return doctorElasticsearchRepository.findByTagsIn(tags);
    }
    
    /**
     * Buscar por rango de experiencia
     */
    public List<DoctorElasticsearch> findByExperienceYearsBetween(int minYears, int maxYears) {
        return doctorElasticsearchRepository.findByExperienceYearsBetween(minYears, maxYears);
    }
    
    /**
     * Buscar por rango de rating
     */
    public List<DoctorElasticsearch> findByRatingBetween(double minRating, double maxRating) {
        return doctorElasticsearchRepository.findByRatingBetween(minRating, maxRating);
    }
}
