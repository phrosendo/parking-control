package com.api.parking_control.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.api.parking_control.dtos.ParkingSpotDTO;
import com.api.parking_control.models.ParkingSpotModel;
import com.api.parking_control.services.ParkingSpotService;

import jakarta.validation.Valid;


@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/vaga")
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
	@PostMapping("/cadastrar")
	public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDTO parkingSpotDTO) {
		var parkingSpotModel = new ParkingSpotModel();
		
		// Verifica se a placa do veículo já está cadastrada em alguma Vaga.
		if (parkingSpotService.existsByLicensePlateCar(parkingSpotDTO.getLicensePlateCar())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro: A placa deste veículo já está cadastrada!");
		}
		// Verifica se a vaga não está ocupada através do Número da Vaga.
		else if (parkingSpotService.existsByParkingSpotNumber(parkingSpotDTO.getParkingSpotNumber())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Erro: A vaga de estacionamento já está em uso!");
		}
		// Verifica se a vaga já foi registrada para o Apartamento e Bloco informados.
		else if (parkingSpotService.existsByApartmentAndBlock(parkingSpotDTO.getApartment(),
				parkingSpotDTO.getBlock())) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body("Erro: Vaga de estacionamento já cadastrada para este apartamento/bloco!");
		} else {
			// Converte o DTO em um Model para inserir na tabela.
			BeanUtils.copyProperties(parkingSpotDTO, parkingSpotModel);
			parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
			return ResponseEntity.status(HttpStatus.CREATED).body("Cadastrado:" + parkingSpotService.save(parkingSpotModel).toString());
		}
	}

	/**
	 * Este método retorna uma lista com todas as vagas cadastradas no Banco de
	 * Dados.
	 * 
	 * @return
	 */
	@GetMapping("/listar")
	public ResponseEntity<List<ParkingSpotModel>> getAllParkingSpots() {
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll());
	}

	/**
	 * Esse método faz a busca de uma vaga pelo ID.
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/listar/{id}")
	public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value = "id") Integer id) {
		Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
		if (!parkingSpotModelOptional.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro: Não foi possível encontrar o cadastro desta vaga!");
		}
		return ResponseEntity.status(HttpStatus.OK).body((parkingSpotModelOptional.get()));
	}

	@DeleteMapping("/excluir/{id}")
    public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value = "id")Integer id){
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
        if (!parkingSpotModelOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro: Não foi possível encontrar o cadastro desta vaga!");
        }
        
        parkingSpotService.delete(parkingSpotModelOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("Vaga de estacionamento excluída com sucesso!.");
    }

	@PutMapping("/atualizar/{id}")
    public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") Integer id,
    												@RequestBody @Valid ParkingSpotDTO parkingSpotDTO){
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
        if(!parkingSpotModelOptional.isPresent()) {
        	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erro: Não foi possível encontrar o cadastro desta vaga!");
        }
        
        var parkingSpotModel = new ParkingSpotModel();
        
        BeanUtils.copyProperties(parkingSpotDTO, parkingSpotModel);
        parkingSpotModel.setId(parkingSpotModelOptional.get().getId());
        parkingSpotModel.setRegistrationDate(parkingSpotModelOptional.get().getRegistrationDate());
        
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
    													
    }
	
	/**
	 * Este método é responsável por retornar no body as mensagens de erro
	 * 400 BAD REQUEST. Com isso facilita a visualização do erro retornado.  
	 * @param ex
	 * @return
	 */
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Map<String, String> handleValidationExceptionBadRequest(MethodArgumentNotValidException ex){
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
	    });
		return errors;
	}
}
