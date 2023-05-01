package com.api.parking_control.dtos;

import org.hibernate.validator.constraints.br.CPF;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ParkingSpotDTO {

    @NotBlank(message = "O preenchimento do Número da Vaga é obrigatório!")
    private String parkingSpotNumber;
    @NotBlank(message = "O preenchimento da Placa do veículo é obrigatório!")
    @Size(max = 7, message = "Placa com valor acima de 7 caracteres!")
    private String licensePlateCar;
    @NotBlank(message = "O preenchimento da Marca do veículo é obrigatório!")
    private String brandCar;
    @NotBlank(message = "O preenchimento do Modelo do veículo é obrigatório")
    private String modelCar;
    @NotBlank(message = "O preenchiemnto da Cor do veículo é obrigatório!")
    private String colorCar;
    @NotBlank(message = "O preenchimento do Nome do Responsável pelo veículo é obrigatório!")
    private String responsibleName;
    @NotBlank(message = "O preenchimento do Apartamento é obrigatório!")
    private String apartment;
    @NotBlank(message = "O preenchimento do Bloco é obrigatório!")
    private String block;
    @Email(message = "Email com preenchimento inválido")
    @NotBlank(message = "O preenchimento do E-mail é obrigatório!")
    private String email;
    @CPF(message = "CPF com preenchimento inválido!") 
    @NotBlank(message = "O preenchimento do CPF é obrigatório!")
    private String cpf;
}
