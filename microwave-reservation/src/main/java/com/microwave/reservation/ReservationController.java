package com.microwave.reservation;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.time.*;

@CrossOrigin(
  origins = {
    "https://microwavereservationpages.dev"
    }
  )
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationRepository repo;

    public ReservationController(ReservationRepository repo) {
        this.repo = repo;
    }

    // 全件取得
    @GetMapping
    public List<Reservation> getAll() {
        return repo.findAll();
    }

    // 予約登録
    @PostMapping
    public Reservation create(@RequestBody Reservation r) {
        r.setStatus("WAITING");
        r.setCreatedAt(LocalDateTime.now());
        return repo.save(r);
    }

    // 次の人を開始
    @PostMapping("/start")
    public Reservation startNext() {

        // 既に使用中の人がいればその人を返す（事故防止）
        Reservation using = repo.findFirstByStatusOrderByCreatedAtAsc("USING");
        if (using != null) {
            return using;
        }

        // WAITINGの中で一番古い人を取得
        Reservation next = repo.findFirstByStatusOrderByCreatedAtAsc("WAITING");

        if (next != null) {
            next.setStatus("USING");
            next.setStartTime(LocalDateTime.now());
            return repo.save(next);
        }

        return null;
    }

    // 現在使用中の人
    @GetMapping("/current")
    public Reservation current() {
        return repo.findFirstByStatusOrderByCreatedAtAsc("USING");
    }

    // 待機リスト
    @GetMapping("/waiting")
    public List<Reservation> waiting() {
        return repo.findAll()
                .stream()
                .filter(r -> "WAITING".equals(r.getStatus()))
                .toList();
    }

    // 完了
    @PostMapping("/{id}/complete")
    public Reservation complete(@PathVariable Long id) {
        Reservation r = repo.findById(id).orElseThrow();
        r.setStatus("DONE");
        return repo.save(r);
    }

    // 全削除（リセット）
    @PostMapping("/reset")
    public void reset(@RequestParam String password){

      if(!"1234".equals(password)){
        throw new RuntimeException("Invalid password");
      }

      repo.deleteAll();
    }
}


