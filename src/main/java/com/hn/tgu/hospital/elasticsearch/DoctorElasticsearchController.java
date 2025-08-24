package com.hn.tgu.hospital.elasticsearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/elasticsearch/doctors")
@CrossOrigin(origins = "*")
public class DoctorElasticsearchController {
    
    @Autowired
    private DoctorElasticsearchService doctorElasticsearchService;
    
    /**
     * Búsqueda con facets
     * GET /api/elasticsearch/doctors/search-with-facets
     */
    @GetMapping("/search-with-facets")
    public ResponseEntity<Map<String, Object>> searchWithFacets(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String hospital,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Integer maxExperience,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Map<String, Object> result = doctorElasticsearchService.searchWithFacets(
                query, specialty, hospital, minExperience, maxExperience, 
                minRating, maxRating, available, tags, page, size);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error en búsqueda con facets: " + e.getMessage()));
        }
    }
    
    /**
     * Búsqueda por texto
     * GET /api/elasticsearch/doctors/search
     */
    @GetMapping("/search")
    public ResponseEntity<Page<DoctorElasticsearch>> searchByText(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Page<DoctorElasticsearch> result = doctorElasticsearchService.searchByText(query, page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Búsqueda avanzada
     * GET /api/elasticsearch/doctors/search-advanced
     */
    @GetMapping("/search-advanced")
    public ResponseEntity<Page<DoctorElasticsearch>> searchAdvanced(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String hospital,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Integer maxExperience,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Page<DoctorElasticsearch> result = doctorElasticsearchService.searchAdvanced(
                query, specialty, hospital, minExperience, maxExperience, 
                minRating, maxRating, available, page, size);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener todas las especialidades
     * GET /api/elasticsearch/doctors/specialties
     */
    @GetMapping("/specialties")
    public ResponseEntity<List<String>> getAllSpecialties() {
        try {
            List<String> specialties = doctorElasticsearchService.getAllSpecialties();
            return ResponseEntity.ok(specialties);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener todos los hospitales
     * GET /api/elasticsearch/doctors/hospitals
     */
    @GetMapping("/hospitals")
    public ResponseEntity<List<String>> getAllHospitals() {
        try {
            List<String> hospitals = doctorElasticsearchService.getAllHospitals();
            return ResponseEntity.ok(hospitals);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener todos los tags
     * GET /api/elasticsearch/doctors/tags
     */
    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAllTags() {
        try {
            List<String> tags = doctorElasticsearchService.getAllTags();
            return ResponseEntity.ok(tags);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Sincronizar doctor desde JPA
     * POST /api/elasticsearch/doctors/sync
     */
    @PostMapping("/sync")
    public ResponseEntity<DoctorElasticsearch> syncDoctor(@RequestBody com.hn.tgu.hospital.entity.Doctor doctor) {
        try {
            DoctorElasticsearch doctorES = doctorElasticsearchService.syncFromJPA(doctor);
            return ResponseEntity.ok(doctorES);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener doctor por ID
     * GET /api/elasticsearch/doctors/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorElasticsearch> getDoctorById(@PathVariable String id) {
        try {
            return doctorElasticsearchService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener todos los doctores
     * GET /api/elasticsearch/doctors
     */
    @GetMapping
    public ResponseEntity<List<DoctorElasticsearch>> getAllDoctors() {
        try {
            List<DoctorElasticsearch> doctors = doctorElasticsearchService.findAll();
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Búsqueda por especialidad
     * GET /api/elasticsearch/doctors/specialty/{specialty}
     */
    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<List<DoctorElasticsearch>> getDoctorsBySpecialty(@PathVariable String specialty) {
        try {
            List<DoctorElasticsearch> doctors = doctorElasticsearchService.findBySpecialty(specialty);
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Búsqueda por hospital
     * GET /api/elasticsearch/doctors/hospital/{hospital}
     */
    @GetMapping("/hospital/{hospital}")
    public ResponseEntity<List<DoctorElasticsearch>> getDoctorsByHospital(@PathVariable String hospital) {
        try {
            List<DoctorElasticsearch> doctors = doctorElasticsearchService.findByHospital(hospital);
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Búsqueda por disponibilidad
     * GET /api/elasticsearch/doctors/available/{available}
     */
    @GetMapping("/available/{available}")
    public ResponseEntity<List<DoctorElasticsearch>> getDoctorsByAvailability(@PathVariable boolean available) {
        try {
            List<DoctorElasticsearch> doctors = doctorElasticsearchService.findByAvailable(available);
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Búsqueda por tags
     * POST /api/elasticsearch/doctors/tags
     */
    @PostMapping("/tags")
    public ResponseEntity<List<DoctorElasticsearch>> getDoctorsByTags(@RequestBody List<String> tags) {
        try {
            List<DoctorElasticsearch> doctors = doctorElasticsearchService.findByTagsIn(tags);
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Búsqueda por rango de experiencia
     * GET /api/elasticsearch/doctors/experience
     */
    @GetMapping("/experience")
    public ResponseEntity<List<DoctorElasticsearch>> getDoctorsByExperienceRange(
            @RequestParam int minYears,
            @RequestParam int maxYears) {
        try {
            List<DoctorElasticsearch> doctors = doctorElasticsearchService.findByExperienceYearsBetween(minYears, maxYears);
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Búsqueda por rango de rating
     * GET /api/elasticsearch/doctors/rating
     */
    @GetMapping("/rating")
    public ResponseEntity<List<DoctorElasticsearch>> getDoctorsByRatingRange(
            @RequestParam double minRating,
            @RequestParam double maxRating) {
        try {
            List<DoctorElasticsearch> doctors = doctorElasticsearchService.findByRatingBetween(minRating, maxRating);
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
