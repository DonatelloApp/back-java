package com.igrowker.donatello.services;

import com.igrowker.donatello.dtos.finances.FinanceDTO;
import com.igrowker.donatello.dtos.finances.FinanceIncomeDto;
import org.springframework.http.HttpHeaders;

import java.util.List;

public interface IFinancesService {
    List<FinanceDTO> getAll(HttpHeaders headers);
    FinanceDTO create(HttpHeaders headers, FinanceDTO dto);
    FinanceDTO edit(HttpHeaders headers, Integer id, FinanceDTO dto);
    void delete(HttpHeaders headers, Integer id);
    FinanceDTO getById(HttpHeaders headers, Integer id);
    List<FinanceDTO> getReports(HttpHeaders headers);
    FinanceIncomeDto getIncomes(HttpHeaders headers);


}
