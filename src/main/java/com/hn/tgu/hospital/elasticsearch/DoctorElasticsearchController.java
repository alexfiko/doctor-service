package com.hn.tgu.hospital.elasticsearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;

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
     * GET /api/elasticsearch/doctors/sync
     * ESSENCIAL para crear índices y sincronizar datos
     * Se puede probar desde el navegador
     */
    @GetMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncDoctor() {
        try {
            // Crear un doctor de ejemplo para sincronizar
            com.hn.tgu.hospital.entity.Doctor doctor = new com.hn.tgu.hospital.entity.Doctor();
            doctor.setId("sync-test");
            doctor.setName("Dr. Sincronización Test");
            doctor.setSpecialty("Medicina General");
            doctor.setImg("default.jpg");
            doctor.setExperienceYears(5);
            doctor.setRating(4.5);
            doctor.setHospital("Hospital Test");
            doctor.setAvailable(true);
            doctor.setDescription("Doctor para probar sincronización con Elasticsearch");
            doctor.setTags(Arrays.asList("test", "sync"));
            doctor.setDiasLaborales(Arrays.asList("Lunes", "Martes", "Miércoles"));
            doctor.setHorarioEntrada("08:00");
            doctor.setHorarioSalida("17:00");
            doctor.setDuracionCita(30);
            doctor.setHorariosDisponibles(new HashMap<>());
            
            DoctorElasticsearch doctorES = doctorElasticsearchService.syncFromJPA(doctor);
            
            // Retornar respuesta con información del sync
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Doctor sincronizado correctamente (modo test)");
            response.put("doctor", doctorES);
            response.put("status", "success");
            response.put("note", "Los datos no se guardan en Elasticsearch hasta resolver dependencias");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error en sincronización: " + e.getMessage());
            errorResponse.put("status", "error");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Obtener doctor por ID (usando búsqueda por texto)
     * GET /api/elasticsearch/doctors/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorElasticsearch> getDoctorById(@PathVariable String id) {
        try {
            // Buscar por ID usando búsqueda por texto
            Page<DoctorElasticsearch> page = doctorElasticsearchService.searchByText(id, 0, 1);
            if (!page.getContent().isEmpty()) {
                return ResponseEntity.ok(page.getContent().get(0));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Obtener todos los doctores
     * GET /api/elasticsearch/doctors
     */
    @GetMapping
    public ResponseEntity<Page<DoctorElasticsearch>> getAllDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Page<DoctorElasticsearch> doctors = doctorElasticsearchService.findAll(page, size);
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
