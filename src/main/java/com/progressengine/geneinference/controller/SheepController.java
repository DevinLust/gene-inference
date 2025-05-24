package com.progressengine.geneinference.controller;

import com.progressengine.geneinference.dto.SheepRequestDTO;
import com.progressengine.geneinference.dto.SheepResponseDTO;
import com.progressengine.geneinference.model.Sheep;
import com.progressengine.geneinference.service.SheepService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/sheep")
public class SheepController {

    private final SheepService sheepService;

    public SheepController(SheepService sheepService) {
        this.sheepService = sheepService;
    }

    @PostMapping(consumes = {"application/json", "application/json;charset=UTF-8"})
    public String addSheep(@RequestBody SheepRequestDTO sheepRequestDTO) {
        Sheep sheep = sheepService.fromRequestDTO(sheepRequestDTO);
        Sheep savedSheep = sheepService.saveSheep(sheep);

        return "Added New Sheep: " + savedSheep.getName();
    }

    @GetMapping
    public List<SheepResponseDTO> getAllSheep() {
        List<Sheep> sheepList = sheepService.getAllSheep();
        return sheepList.stream()
                .map(sheepService::toResponseDTO)
                .toList();
    }

}
