package com.igrowker.donatello.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.igrowker.donatello.dtos.finances.*;
import com.igrowker.donatello.exceptions.ForbiddenException;
import com.igrowker.donatello.exceptions.NotFoundException;
import com.igrowker.donatello.mappers.FinanceMapper;
import com.igrowker.donatello.services.IAuthService;
import com.igrowker.donatello.services.IFinancesService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FinancesServiceImpl implements IFinancesService {
    @Autowired
    private Environment environment;

    final String userPythonApi = "davidDonatelloPython"; // TODO PASAR A properties con valores correctos!
    final String passPythonApi = "davidDonatelloPython"; // TODO PASAR A properties con valores correctos!

    private final RestTemplate restTemplate;

    @Autowired
    FinanceMapper financeMapper;
    @Autowired
    IAuthService authService;


    private String getUrlApiPython() {

        return "http://127.0.0.1:8000//api";
        // return environment.getProperty("api.backend.python")+"/api";
    }

    private HttpHeaders getNewHeadersWithAuth() {
        HttpHeaders pythonHeaders = new HttpHeaders();
        pythonHeaders.setContentType(MediaType.APPLICATION_JSON);
        // todo verificar si necesitamos credenciales.. deberiamos !!! => pythonHeaders.add("Authorization", "Basic "+generateHash());
        return pythonHeaders;
    }

    private String generateHash() {
        String credentials = userPythonApi + ":" + passPythonApi;
        return new String(Base64.getEncoder().encode(credentials.getBytes()));
    }

    @Override
    public List<FinanceDTO> getAll(HttpHeaders headers) {
        Integer userId = authService.getLoguedUser(headers).getId();
        ResponseEntity<String> rawResp = restTemplate.exchange(
                getUrlApiPython() + "/finances/transactions/" + userId + "/",// +userId, // => todo deben recibir el id user, para traer solo los que corresponde a un user en particular
                HttpMethod.GET,
                new HttpEntity<>(getNewHeadersWithAuth()),
                String.class
        );
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            List<FinanceExternalDto> financeExternalDtoList = objectMapper.readValue(rawResp.getBody(), new TypeReference<List<FinanceExternalDto>>() {
            });
            return financeMapper.toFinanceDtoList(financeExternalDtoList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FinanceDTO create(HttpHeaders headers, FinanceDTO dto) {
        Integer userId = authService.getLoguedUser(headers).getId();
        dto.setUserID(userId);

        // Mapear FinanceDTO a FinanceExternalDto
        FinanceExternalDto externalDto = financeMapper.toFinanceExternalDto(dto);

        ResponseEntity<FinanceExternalDto> resp = restTemplate.postForEntity(
                getUrlApiPython() + "/finances/create/",
                new HttpEntity<>(externalDto,getNewHeadersWithAuth()),
                FinanceExternalDto.class);

        // Mapear de vuelta a FinanceDTO
        return financeMapper.toFinanceDto(Objects.requireNonNull(resp.getBody()));
    }

    @Override
    public FinanceDTO edit(HttpHeaders headers, Integer id, FinanceDTO dto) {

        dto.setId(id);
        Integer userId = authService.getLoguedUser(headers).getId();

        // Obtener la información del recurso para verificar la propiedad
        ResponseEntity<FinanceExternalDto> getResponse = restTemplate.exchange(
                getUrlApiPython() + "/finances/details/" + id + "/",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                FinanceExternalDto.class
        );

        FinanceExternalDto externalDto = getResponse.getBody();
        if (externalDto == null || !externalDto.getId_usuario().equals(userId)) {
            throw new ForbiddenException("The user cannot update someone else's finance!");
        }

        // Mapear FinanceDTO a FinanceExternalDto
        FinanceExternalDto externalDTO = financeMapper.updateFinance(dto, externalDto);

        // Realizar la solicitud de actualización
        HttpEntity<FinanceExternalDto> updateRequest = new HttpEntity<>(externalDTO, headers);
        ResponseEntity<FinanceExternalDto> updateResponse = restTemplate.exchange(
                getUrlApiPython() + "/finances/update/" + id + "/",
                HttpMethod.PUT,
                new HttpEntity<>(externalDTO, headers),
                FinanceExternalDto.class
        );

        return financeMapper.toFinanceDto(Objects.requireNonNull(updateResponse.getBody()));

    }

    @Override
    public void delete(HttpHeaders headers, Integer id) {
        Integer userId = authService.getLoguedUser(headers).getId();
        // Obtener la información del recurso para verificar la propiedad
        ResponseEntity<FinanceExternalDto> getResponse = restTemplate.exchange(
                getUrlApiPython() + "/finances/details/" + id + "/",
                HttpMethod.GET,
                new HttpEntity<>(getNewHeadersWithAuth()),
                FinanceExternalDto.class
        );

        FinanceExternalDto dto = getResponse.getBody();
        if (dto == null || !dto.getId_usuario().equals(userId)) {
            throw new ForbiddenException("The user cannot delete someone else's finance!");
        }

        // Realizar la solicitud de eliminación si el usuario es el propietario
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                getUrlApiPython() + "/finances/delete/" + id + "/",
                HttpMethod.DELETE,
                new HttpEntity<>(getNewHeadersWithAuth()),
                Void.class
        );

    }

    @Override
    public FinanceDTO getById(HttpHeaders headers, Integer id) {
        Integer userId = authService.getLoguedUser(headers).getId();
        ResponseEntity<FinanceExternalDto> resp = restTemplate.exchange(
                getUrlApiPython() + "/finances/details/" + id +"/", // => todo deben recibir el id user, para verificar si es correcto devolver info..
                HttpMethod.GET,
                new HttpEntity<>(getNewHeadersWithAuth()),
                FinanceExternalDto.class
        );
        FinanceExternalDto dto = resp.getBody();
        if (dto == null || !dto.getId_usuario().equals(userId)) {
            throw new ForbiddenException("User cannot see another user's finances!");
        }
        return financeMapper.toFinanceDto(resp.getBody());
    }

    @Override
    public List<FinanceDTO> getReports(HttpHeaders headers) {
        return null;
    }

    @Override
    public FinanceIncomeDto getIncomes(HttpHeaders headers) {
        Integer userId = authService.getLoguedUser(headers).getId();

        ResponseEntity<FinanceIncomeExternalDto> response = restTemplate.getForEntity(
                getUrlApiPython() + "/finances/ingreso-total/" + userId + "/",
                FinanceIncomeExternalDto.class
        );

        return financeMapper.toFinanceIncomeDto(Objects.requireNonNull(response.getBody()));
    }
}