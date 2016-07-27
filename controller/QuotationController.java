package com.yiilab.inside.controllers.v1.json;

import com.yiilab.inside.dao.ExchangeRepository;
import com.yiilab.inside.dao.FinancialInstrumentRepository;
import com.yiilab.inside.dao.user.FavoriteInstrumentRepository;
import com.yiilab.inside.model.Category;
import com.yiilab.inside.model.ExchangeType;
import com.yiilab.inside.model.FavoriteInstrument;
import com.yiilab.inside.model.FinancialInstrument;
import com.yiilab.inside.model.user.User;
import com.yiilab.inside.records.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/json/quotation")
public class QuotationController extends AbstractController{

    private final ExchangeRepository exchangeRepository;
    private final FinancialInstrumentRepository financialInstrumentRepository;
    private final FavoriteInstrumentRepository favoriteInstrumentRepository;


    @Autowired
    public QuotationController(
            ExchangeRepository exchangeRepository,
            FinancialInstrumentRepository financialInstrumentRepository,
            FavoriteInstrumentRepository favoriteInstrumentRepository) {
        this.exchangeRepository = exchangeRepository;
        this.financialInstrumentRepository = financialInstrumentRepository;
        this.favoriteInstrumentRepository = favoriteInstrumentRepository;
    }

    @RequestMapping("exchanges")
    public ExchangeRecord getListExchange(){
        return new ExchangeRecord(
                exchangeRepository.countByHidden(false),
                exchangeRepository.findByHidden(false));
    }

    @RequestMapping("quote-types")
    public CategoryListRecord getCategories() {
        return new CategoryListRecord();
    }

    @RequestMapping("/{id}/quotes")
    public FinancialInstrumentListRecord getList(
            @PathVariable Integer id,
            @RequestParam(value = "q", required = false, defaultValue = "") String search,
            @RequestParam(value = "count", defaultValue = "10", required = false) Integer count,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page
    ){
        final String query = "%" + search + "%";
        final Category category = Category.findById(id);
        return new FinancialInstrumentListRecord(
                financialInstrumentRepository.countByExchangeTypeAndNameContainingAndCategory(
                        ExchangeType.BTC,
                        query,
                        category
                ),
                financialInstrumentRepository.findByExchangeTypeAndNameContainingAndCategory(
                        ExchangeType.BTC,
                        query,
                        category,
                        new PageRequest(page,count))
        );
    }

    @RequestMapping("/{id}")
    public FinancialInstrumentRecord getQuote(
            @PathVariable Long id
    ){
        return new FinancialInstrumentRecord(
                financialInstrumentRepository.findOne(id)
        );
    }

    @RequestMapping(value = "favourite", method = RequestMethod.GET)
    public ResponseEntity<?> getFavorites(
            @RequestParam(value = "q", required = false, defaultValue = "") String search,
            @RequestParam(value = "count", defaultValue = "10", required = false) Integer countInPage,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page
    ){
        final User user = getCurrentUser();
        final List<FavoriteInstrument> favoriteInstruments =
                favoriteInstrumentRepository
                        .findByUserAndFinancialInstrumentNameContainingOrderByOrderInList(user, search, new PageRequest(page, countInPage));
        final Long count = favoriteInstrumentRepository.countByUserAndFinancialInstrumentNameContaining(user, search);
        return ResponseEntity.ok(new FavoriteInstrumentListRecord(count, favoriteInstruments));
    }

    @RequestMapping(value = "favourite", method = RequestMethod.PUT)
    public ResponseEntity<?> addToFavorites(
            @RequestBody FinancialInstrumentRecord financialInstrumentRecord
    ){
        final FinancialInstrument financialInstrument = financialInstrumentRepository.findOne(financialInstrumentRecord.getId());
        final User user = getCurrentUser();
        if (financialInstrument == null) {
            return ResponseEntity.notFound().build();
        }
        final FavoriteInstrument favoriteInstrument = favoriteInstrumentRepository.findTopByUserOrderByOrderInListDesc(user);
        Long order = 0L;
        if (favoriteInstrument != null) {
            order = favoriteInstrument.getOrderInList() + 1L;
        }
        final FavoriteInstrument newFavoriteInstrument = new FavoriteInstrument(user, financialInstrument, order);
        favoriteInstrumentRepository.save(newFavoriteInstrument);

        return ResponseEntity.ok(new FavoriteInstrumentRecord(newFavoriteInstrument));
    }

    @RequestMapping(value = "favourite", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteFavorites(
            @RequestBody FavoriteInstrumentRecord favoriteInstrumentRecord
    ){
        final FavoriteInstrument favoriteInstrument = favoriteInstrumentRepository.findOne(favoriteInstrumentRecord.getId());
        if (favoriteInstrument.getUser().equals(getCurrentUser())) {
            favoriteInstrumentRepository.delete(favoriteInstrument);
        }
        return ResponseEntity.ok(new SuccessResponse());
    }

    @RequestMapping(value = "favourite", method = RequestMethod.POST)
    public ResponseEntity<?> updateOrderFavorites(
            @RequestBody FavoriteInstrumentRecord favoriteInstrumentRecord
    ){
        if (favoriteInstrumentRecord.getId() == null
                || favoriteInstrumentRecord.getOrder() == null) {
            return ResponseEntity.ok("data bullshit");
        }
        final FavoriteInstrument favoriteInstrument = favoriteInstrumentRepository.findOne(favoriteInstrumentRecord.getId());
        if (favoriteInstrument == null) {
            return ResponseEntity.ok("favoriteInstrument bullshit");
        }
        final List<FavoriteInstrument> favoriteInstruments
                = favoriteInstrumentRepository
                .findByUserAndOrderInListGreaterThanEqualOrderByOrderInList(getCurrentUser(),
                        favoriteInstrumentRecord.getOrder());
        Long odOrder = favoriteInstrumentRecord.getOrder() + 1;
        for (FavoriteInstrument instrument : favoriteInstruments) {
            instrument.setOrderInList(odOrder);
            favoriteInstrumentRepository.save(instrument);
            odOrder++;
        }
        favoriteInstrument.setOrderInList(favoriteInstrumentRecord.getOrder());
        favoriteInstrumentRepository.save(favoriteInstrument);
        return ResponseEntity.ok(new SuccessResponse());
    }
}
