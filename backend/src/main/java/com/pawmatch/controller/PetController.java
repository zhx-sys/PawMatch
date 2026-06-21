package com.pawmatch.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pawmatch.dto.request.AddPetRequest;
import com.pawmatch.dto.request.UpdatePetRequest;
import com.pawmatch.dto.request.PetQueryRequest;
import com.pawmatch.dto.request.PetSearchRequest;
import com.pawmatch.dto.response.ApiResponse;
import com.pawmatch.dto.response.PetResponse;
import com.pawmatch.dto.response.PetDetailResponse;
import com.pawmatch.exception.BusinessException;
import com.pawmatch.security.PawMatchPrincipal;
import com.pawmatch.service.PetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pet")
public class PetController {

    private final PetService petService;

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    private String absoluteUploadPath;

    @PostConstruct
    public void init() {
        this.absoluteUploadPath = Paths.get(uploadPath).toAbsolutePath().normalize().toString();
        new File(absoluteUploadPath, "pets").mkdirs();
    }

    public PetController(PetService petService) {
        this.petService = petService;
    }

    /**
     * 登录页轮播：返回状态为"可领养"且有图片的宠物（无需登录）
     */
    @GetMapping("/carousel")
    public ApiResponse<List<PetResponse>> getCarouselPets() {
        List<PetResponse> list = petService.getCarouselPets();
        return ApiResponse.success(list);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Long> addPet(HttpServletRequest request) {
        return addPetMultipart(request);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Long> addPetJson(@Valid @RequestBody AddPetRequest request) {
        Long shelterId = getCurrentUserId();
        Long id = petService.addPet(request, shelterId);
        return ApiResponse.success("发布成功", id);
    }

    private ApiResponse<Long> addPetMultipart(HttpServletRequest request) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        AddPetRequest addPetRequest = new AddPetRequest();
        addPetRequest.setName(getParam(multipartRequest, "name"));
        addPetRequest.setType(getParam(multipartRequest, "type"));
        addPetRequest.setBreed(getParam(multipartRequest, "breed"));
        addPetRequest.setGender(getParam(multipartRequest, "gender"));
        addPetRequest.setColor(getParam(multipartRequest, "color"));
        addPetRequest.setHealthStatus(getParam(multipartRequest, "healthStatus"));
        addPetRequest.setDescription(getParam(multipartRequest, "description"));

        String ageStr = getParam(multipartRequest, "age");
        if (ageStr != null && !ageStr.isEmpty()) {
            addPetRequest.setAge(Integer.parseInt(ageStr));
        }

        String weightStr = getParam(multipartRequest, "weight");
        if (weightStr != null && !weightStr.isEmpty()) {
            addPetRequest.setWeight(Double.parseDouble(weightStr));
        }

        String vaccinatedStr = getParam(multipartRequest, "vaccinated");
        if (vaccinatedStr != null && !vaccinatedStr.isEmpty()) {
            addPetRequest.setVaccinated(Boolean.parseBoolean(vaccinatedStr));
        }

        String sterilizedStr = getParam(multipartRequest, "sterilized");
        if (sterilizedStr != null && !sterilizedStr.isEmpty()) {
            addPetRequest.setSterilized(Boolean.parseBoolean(sterilizedStr));
        }

        // 匹配画像相关字段
        addPetRequest.setSizeLevel(getParam(multipartRequest, "sizeLevel"));
        addPetRequest.setActivityLevel(getParam(multipartRequest, "activityLevel"));

        String beginnerFriendlyStr = getParam(multipartRequest, "beginnerFriendly");
        if (beginnerFriendlyStr != null && !beginnerFriendlyStr.isEmpty()) {
            addPetRequest.setBeginnerFriendly(Boolean.parseBoolean(beginnerFriendlyStr));
        }

        String goodWithKidsStr = getParam(multipartRequest, "goodWithKids");
        if (goodWithKidsStr != null && !goodWithKidsStr.isEmpty()) {
            addPetRequest.setGoodWithKids(Boolean.parseBoolean(goodWithKidsStr));
        }

        String goodWithPetsStr = getParam(multipartRequest, "goodWithPets");
        if (goodWithPetsStr != null && !goodWithPetsStr.isEmpty()) {
            addPetRequest.setGoodWithPets(Boolean.parseBoolean(goodWithPetsStr));
        }

        // Validate required fields
        validateRequest(addPetRequest);

        // Handle image uploads
        List<MultipartFile> files = multipartRequest.getFiles("images");
        List<String> imageUrls = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    imageUrls.add(saveImage(file));
                }
            }
        }
        addPetRequest.setImages(imageUrls);

        Long shelterId = getCurrentUserId();
        Long id = petService.addPet(addPetRequest, shelterId);
        return ApiResponse.success("发布成功", id);
    }

    private String getParam(MultipartHttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return (value != null) ? value.trim() : null;
    }

    private void validateRequest(AddPetRequest req) {
        List<String> errors = new ArrayList<>();
        if (req.getName() == null || req.getName().isEmpty()) errors.add("宠物名称不能为空");
        if (req.getType() == null || req.getType().isEmpty()) errors.add("宠物类型不能为空");
        if (req.getBreed() == null || req.getBreed().isEmpty()) errors.add("品种不能为空");
        if (req.getGender() == null || req.getGender().isEmpty()) errors.add("性别不能为空");
        if (req.getAge() == null) errors.add("年龄不能为空");
        if (req.getHealthStatus() == null || req.getHealthStatus().isEmpty()) errors.add("健康状况不能为空");
        if (req.getVaccinated() == null) errors.add("疫苗接种情况不能为空");
        if (req.getSterilized() == null) errors.add("绝育情况不能为空");
        if (!errors.isEmpty()) {
            throw new BusinessException(400, String.join("; ", errors));
        }
    }

    private String saveImage(MultipartFile file) {
        try {
            Path uploadDir = Paths.get(absoluteUploadPath, "pets");
            Files.createDirectories(uploadDir);

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path targetPath = uploadDir.resolve(filename);
            file.transferTo(targetPath.toFile());

            return "/uploads/pets/" + filename;
        } catch (IOException e) {
            throw new BusinessException(500, "图片保存失败: " + e.getMessage());
        }
    }

    @PutMapping("/{petId}")
    public ApiResponse<Void> updatePet(@PathVariable Long petId, @Valid @RequestBody UpdatePetRequest request) {
        petService.updatePet(request, petId);
        return ApiResponse.success("更新成功", null);
    }

    @DeleteMapping("/{petId}")
    public ApiResponse<Void> deletePet(@PathVariable Long petId) {
        Long shelterId = getCurrentUserId();
        petService.deletePet(petId, shelterId);
        return ApiResponse.success("下架成功", null);
    }

    @PostMapping("/{petId}/restore")
    public ApiResponse<Void> restorePet(@PathVariable Long petId) {
        Long shelterId = getCurrentUserId();
        petService.restorePet(petId, shelterId);
        return ApiResponse.success("上架成功", null);
    }

    @GetMapping("/list")
    public ApiResponse<IPage<PetResponse>> getPetList(@ModelAttribute PetQueryRequest request) {
        Long shelterId = getCurrentUserId();
        IPage<PetResponse> page = petService.getPetList(request, shelterId);
        return ApiResponse.success(page);
    }

    @GetMapping("/search")
    public ApiResponse<IPage<PetResponse>> searchPets(@ModelAttribute PetSearchRequest request) {
        IPage<PetResponse> page = petService.searchPets(request);
        return ApiResponse.success(page);
    }

    @GetMapping("/{petId}")
    public ApiResponse<PetDetailResponse> getPetDetail(@PathVariable Long petId) {
        PetDetailResponse detail = petService.getPetDetail(petId);
        return ApiResponse.success(detail);
    }

    @GetMapping("/recommend")
    public ApiResponse<List<PetResponse>> recommend(@RequestParam Long userId) {
        List<PetResponse> recommended = petService.recommendForUser(userId);
        return ApiResponse.success(recommended);
    }

    private Long getCurrentUserId() {
        PawMatchPrincipal principal = (PawMatchPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.getUserId();
    }
}
