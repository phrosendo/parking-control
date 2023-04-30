package com.api.parking_control.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.parking_control.dtos.ParkingSpotDTO;
import com.api.parking_control.models.ParkingSpotModel;
import com.api.parking_control.services.ParkingSpotService;

import jakarta.validation.Valid;


@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping(path="/api/vaga")
public class ParkingSpotController {

	final ParkingSpotService parkingSpotService;

	public ParkingSpotController(ParkingSpotService parkingSpotService) {
		this.parkingSpotService = parkingSpotService;
	}

	/**
	 * Este método tem como objetivo realizar o cadastro de uma Vaga de
	 * Estacionamento.
	 * 
	 * @param parkingSpotDTO
	 * @return
	 */
	@PostMapping(path="/cadastrar")
	public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDTO parkingSpotDTO) {
		var parkingSpotModel = new ParkingSpotModel();
		
		// Verifica se a placa do veículo já está cadastrada em alguma Vaga.
		if (parkingSpotService.existsByLicensePlateCar(parkingSpotDTO.getLicensePlateCar())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: License Plate Car is already in use!");
		}
		// Verifica se a vaga não está ocupada através do Número da Vaga.
		else if (parkingSpotService.existsByParkingSpotNumber(parkingSpotDTO.getParkingSpotNumber())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Parking Spot is already in use!");
		}
		// Verifica se a vaga já foi registrada para o Apartamento e Bloco informados.
		else if (parkingSpotService.existsByApartmentAndBlock(parkingSpotDTO.getApartment(),
				parkingSpotDTO.getBlock())) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body("Error: Parking Spot already registered for this apartment/block!");
		} else {
			// Converte o DTO em um Model para inserir na tabela.
			BeanUtils.copyProperties(parkingSpotDTO, parkingSpotModel);
			parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
			return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
		}
	}

	/**
	 * Este método retorna uma lista com todas as vagas cadastradas no Banco de
	 * Dados.
	 * 
	 * @return
	 */
	@GetMapping(path="/listar")
	public ResponseEntity<List<ParkingSpotModel>> getAllParkingSpots() {
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll());
	}

	/**
	 * Esse método faz a busca de uma vaga pelo ID.
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(path="/listar/{id}")
	public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value = "id") Integer id) {
		Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
		if (!parkingSpotModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
		}
		return ResponseEntity.status(HttpStatus.OK).body((parkingSpotModelOptional.get()));
	}

	@DeleteMapping(path="/excluir/{id}")
    public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value = "id")Integer id){
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
        if (!parkingSpotModelOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
        }
        
        parkingSpotService.delete(parkingSpotModelOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("Parking Spot deleted successfuly.");
    }

	@PutMapping(path="/atualizar/{id}")
    public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") Integer id,
    												@RequestBody @Valid ParkingSpotDTO parkingSpotDTO){
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
        if(!parkingSpotModelOptional.isPresent()) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
        }
        
        var parkingSpotModel = new ParkingSpotModel();
        
        BeanUtils.copyProperties(parkingSpotDTO, parkingSpotModel);
        parkingSpotModel.setId(parkingSpotModelOptional.get().getId());
        parkingSpotModel.setRegistrationDate(parkingSpotModelOptional.get().getRegistrationDate());
        
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
    													
    }
}
