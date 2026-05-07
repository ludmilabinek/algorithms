package org.example.algorithms.controller;

import org.example.algorithms.dto.AddListsRequest;
import org.example.algorithms.dto.AddListsResponse;
import org.example.algorithms.dto.FactorialRequest;
import org.example.algorithms.dto.FactorialResponse;
import org.example.algorithms.dto.MaxProfitRequest;
import org.example.algorithms.dto.MaxProfitResponse;
import org.example.algorithms.dto.MinutesBetweenRequest;
import org.example.algorithms.dto.MinutesBetweenResponse;
import org.example.algorithms.dto.MostRepeatedLettersRequest;
import org.example.algorithms.dto.MostRepeatedLettersResponse;
import org.example.algorithms.dto.NoZeroPairRequest;
import org.example.algorithms.dto.NoZeroPairResponse;
import org.example.algorithms.dto.PhoneValidationRequest;
import org.example.algorithms.dto.PhoneValidationResponse;
import org.example.algorithms.dto.RleCompressRequest;
import org.example.algorithms.dto.RleCompressResponse;
import org.example.algorithms.service.AlgorithmsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/algorithms")
public class AlgorithmsController {

    private final AlgorithmsService algorithmsService;

    public AlgorithmsController(AlgorithmsService algorithmsService) {
        this.algorithmsService = algorithmsService;
    }

    @PostMapping("/lists/add")
    public AddListsResponse addLists(@RequestBody AddListsRequest request) {
        List<Integer> merged = algorithmsService.addSortedLists(request.list1(), request.list2());
        return new AddListsResponse(merged);
    }

    @PostMapping("/stocks/max-profit")
    public MaxProfitResponse maxProfit(@RequestBody MaxProfitRequest request) {
        int profit = algorithmsService.maxProfit(request.prices());
        return new MaxProfitResponse(profit);
    }

    @PostMapping("/strings/rle-compress")
    public RleCompressResponse rleCompress(@RequestBody RleCompressRequest request) {
        String compressed = algorithmsService.rleCompress(request.text(), request.caseSensitive());
        return new RleCompressResponse(compressed);
    }

    @PostMapping("/times/minutes-between")
    public MinutesBetweenResponse minutesBetween(@RequestBody MinutesBetweenRequest request) {
        int minutes = algorithmsService.minutesBetween(request.timeRange());
        return new MinutesBetweenResponse(minutes);
    }

    @PostMapping("/strings/most-repeated-letters")
    public MostRepeatedLettersResponse mostRepeatedLetters(@RequestBody MostRepeatedLettersRequest request) {
        String word = algorithmsService.mostRepeatedLetters(request.text());
        return new MostRepeatedLettersResponse(word);
    }

    @PostMapping("/numbers/no-zero-pair")
    public NoZeroPairResponse noZeroPair(@RequestBody NoZeroPairRequest request) {
        List<Integer> pair = algorithmsService.noZeroPair(request.n());
        return new NoZeroPairResponse(pair);
    }

    @PostMapping("/phones/validate")
    public PhoneValidationResponse validatePhone(@RequestBody PhoneValidationRequest request) {
        boolean valid = algorithmsService.isPhoneValid(request.phone());
        return new PhoneValidationResponse(valid);
    }

    @PostMapping("/numbers/factorial")
    public FactorialResponse factorial(@RequestBody FactorialRequest request) {
        String result = algorithmsService.factorial(request.n());
        return new FactorialResponse(result);
    }
}
