package org.example.algorithms.controller;

import org.example.algorithms.dto.AddListsRequest;
import org.example.algorithms.dto.AddListsResponse;
import org.example.algorithms.dto.MaxProfitRequest;
import org.example.algorithms.dto.MaxProfitResponse;
import org.example.algorithms.dto.MinutesBetweenRequest;
import org.example.algorithms.dto.MinutesBetweenResponse;
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
}
