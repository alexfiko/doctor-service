package com.hn.tgu.hospital.elasticsearch;

import com.hn.tgu.hospital.entity.Doctor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DoctorElasticsearchService {
    
    @Autowired
    private DoctorElasticsearchRepository doctorElasticsearchRepository;
    
    /**
     * Búsqueda simple por texto
     */
    public Page<DoctorElasticsearch> searchByText(String query, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return doctorElasticsearchRepository.searchByText(query, pageable);
        } catch (Exception e) {
            // Retornar página vacía en caso de error
            Pageable pageable = PageRequest.of(page, size);
            return Page.empty(pageable);
        }
    }
    
    /**
     * Búsqueda avanzada
     */
    public Page<DoctorElasticsearch> searchAdvanced(String query, String specialty, String hospital, 
                                                   int minExperience, int maxExperience, 
                                                   double minRating, double maxRating, 
                                                   boolean available, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return doctorElasticsearchRepository.searchAdvanced(query, specialty, hospital, 
                                                              minExperience, maxExperience, 
                                                              minRating, maxRating, 
                                                              available, pageable);
        } catch (Exception e) {
            // Retornar página vacía en caso de error
            Pageable pageable = PageRequest.of(page, size);
            return Page.empty(pageable);
        }
    }
    
    /**
     * Obtener todas las especialidades disponibles
     */
    public List<String> getAllSpecialties() {
        try {
            // Usar búsqueda con paginación
            Pageable pageable = PageRequest.of(0, 1000);
            Page<DoctorElasticsearch> page = doctorElasticsearchRepository.searchByText("", pageable);
            
            Set<String> specialties = new HashSet<>();
            for (DoctorElasticsearch doctor : page.getContent()) {
                if (doctor.getSpecialty() != null) {
                    specialties.add(doctor.getSpecialty());
                }
            }
            return new ArrayList<>(specialties);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener todos los hospitales disponibles
     */
    public List<String> getAllHospitals() {
        try {
            // Usar búsqueda con paginación
            Pageable pageable = PageRequest.of(0, 1000);
            Page<DoctorElasticsearch> page = doctorElasticsearchRepository.searchByText("", pageable);
            
            Set<String> hospitals = new HashSet<>();
            for (DoctorElasticsearch doctor : page.getContent()) {
                if (doctor.getHospital() != null) {
                    hospitals.add(doctor.getHospital());
                }
            }
            return new ArrayList<>(hospitals);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener todos los tags disponibles
     */
    public List<String> getAllTags() {
        try {
            // Usar búsqueda con paginación
            Pageable pageable = PageRequest.of(0, 1000);
            Page<DoctorElasticsearch> page = doctorElasticsearchRepository.searchByText("", pageable);
            
            Set<String> allTags = new HashSet<>();
            for (DoctorElasticsearch doctor : page.getContent()) {
                if (doctor.getTags() != null) {
                    allTags.addAll(doctor.getTags());
                }
            }
            return new ArrayList<>(allTags);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Obtener todos los doctores (con paginación)
     */
    public Page<DoctorElasticsearch> findAll(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return doctorElasticsearchRepository.searchByText("", pageable);
        } catch (Exception e) {
            // Retornar página vacía en caso de error
            Pageable pageable = PageRequest.of(page, size);
            return Page.empty(pageable);
        }
    }
    
    /**
     * Búsqueda con facets simplificada
     */
    public Map<String, Object> searchWithFacets(String query, String specialty, String hospital, 
                                               Integer minExperience, Integer maxExperience, 
                                               Double minRating, Double maxRating, 
                                               Boolean available, List<String> tags, 
                                               int page, int size) {
        
        try {
            // Usar búsqueda avanzada del repositorio
            Page<DoctorElasticsearch> doctorPage = searchAdvanced(
                query, specialty, hospital, minExperience, maxExperience, 
                minRating, maxRating, available, page, size);
            
            // Obtener facets básicos
            List<String> allSpecialties = getAllSpecialties();
            List<String> allHospitals = getAllHospitals();
            List<String> allTags = getAllTags();
            
            // Construir respuesta
            Map<String, Object> result = new HashMap<>();
            result.put("doctors", doctorPage.getContent());
            result.put("totalHits", doctorPage.getTotalElements());
            result.put("page", page);
            result.put("size", size);
            
            // Facets simplificados
            Map<String, Object> facets = new HashMap<>();
            facets.put("specialties", allSpecialties);
            facets.put("hospitals", allHospitals);
            facets.put("tags", allTags);
            
            result.put("facets", facets);
            
            return result;
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Error en búsqueda con facets: " + e.getMessage());
            errorResult.put("doctors", new ArrayList<>());
            errorResult.put("facets", new HashMap<>());
            return errorResult;
        }
    }
    
    /**
     * Sincronizar entidad JPA con Elasticsearch
     * Este método es ESSENCIAL para crear índices y sincronizar datos
     * NOTA: Temporalmente sin save() hasta resolver dependencias
     */
    public DoctorElasticsearch syncFromJPA(Doctor doctor) {
        try {
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
            
            // TODO: Implementar save cuando esté disponible
            // Por ahora solo retornamos el objeto creado
            System.out.println("Doctor sincronizado (sin guardar): " + doctorES.getName());
            return doctorES;
        } catch (Exception e) {
            throw new RuntimeException("Error sincronizando doctor con Elasticsearch: " + e.getMessage(), e);
        }
    }
    
    /**
     * Métodos de búsqueda básicos usando Spring Data
     */
    public List<DoctorElasticsearch> findBySpecialty(String specialty) {
        try {
            return doctorElasticsearchRepository.findBySpecialty(specialty);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    public List<DoctorElasticsearch> findByHospital(String hospital) {
        try {
            return doctorElasticsearchRepository.findByHospital(hospital);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    public List<DoctorElasticsearch> findByAvailable(boolean available) {
        try {
            return doctorElasticsearchRepository.findByAvailable(available);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    public List<DoctorElasticsearch> findByTagsIn(List<String> tags) {
        try {
            return doctorElasticsearchRepository.findByTagsIn(tags);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    public List<DoctorElasticsearch> findByExperienceYearsBetween(int minYears, int maxYears) {
        try {
            return doctorElasticsearchRepository.findByExperienceYearsBetween(minYears, maxYears);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    public List<DoctorElasticsearch> findByRatingBetween(double minRating, double maxRating) {
        try {
            return doctorElasticsearchRepository.findByRatingBetween(minRating, maxRating);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
