package com.servlet.sd;

import java.io.IOException;
import java.util.*;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.json.JSONObject;

@RestController
public class Controller {

  // Print de mensagens no Header
  private final String getRecPrint = "Get Ressource";
  private final String freeRecPrint = "Free Ressource";
  private final String checkRecPrint = "Avaliable Ressource";
  private final String busyRecPrint = "Busy Ressource";

  // Identificador do recurso e fila de espera
  private String identifier1 = null;
  private String identifier2 = null;
  private boolean rec1Free = true;
  private boolean rec2Free = true;
  private ArrayList<String> fila1 = new ArrayList<String>();
  private ArrayList<String> fila2 = new ArrayList<String>();

  // Tempo maximo de espera
  private static final int TEMP_MAX = 5000;

  public Map<String, SseEmitter> emitters = new HashMap<String, SseEmitter>();

  // Requisicao do recurso 1
  @CrossOrigin
  @PostMapping("/reqrecCOCA")
  public void reqRec1(@RequestParam String procid) {
    // Verifica se nao e ele que possui e se ja nao esta na fila e se estiver livre
    // consede o recurso
    if (procid.equals(identifier1))
      return;

    if (fila1.contains(procid))
      return;

    if (this.rec1Free) {

      dispatchEventToClient(getRecPrint, "Pegou a COCA", procid);

      // Thread para liberar o recurso por timeout
      new Timer().schedule(new TimerTask() {
        @Override
        public void run() {
          try {
            timeoutRec1(procid);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }, TEMP_MAX);
      this.rec1Free = false;
      this.identifier1 = procid;

      return;
    }

    else {
      fila1.add(procid);
      dispatchEventToClient(busyRecPrint, "Estao com a COCA", procid);
    }
  }

  // Requisicao do recurso 2
  @CrossOrigin
  @PostMapping("/reqrecPEPSI")
  public void reqrec2(@RequestParam String procid) {
    // Verifica se nao e ele que possui e se ja nao esta na fila e se estiver livre
    // consede o recurso
    if (procid.equals(identifier2))
      return;

    if (fila2.contains(procid))
      return;

    if (this.rec2Free) {

      dispatchEventToClient(getRecPrint, "Pegou a PEPSI", procid);

      // Thread para liberar o recurso por timeout
      new Timer().schedule(new TimerTask() {
        @Override
        public void run() {
          try {
            timeoutRec2(procid);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }, TEMP_MAX);
      this.rec2Free = false;
      this.identifier2 = procid;

      return;
    }

    else {
      fila2.add(procid);
      dispatchEventToClient(busyRecPrint, "Estao com a PEPSI", procid);
    }
  }

  // Liberar manualmente o recurso 1
  @CrossOrigin
  @PostMapping("/freerecCOCA")
  public void freerec1(@RequestParam String procid) {
    // Verifica se e ele que possui o recurso
    if (!procid.equals(this.identifier1))
      return;

    dispatchEventToClient(freeRecPrint, "COCA Liberada", procid);

    // Se a fila nao estiver vazia, consede o proximo da fila
    if (fila1.size() > 0) {
      String proximo_cliente = fila1.get(0);
      this.identifier1 = proximo_cliente;
      fila1.remove(0);

      // Thread para liberar o recurso por timeout
      new Timer().schedule(new TimerTask() {
        @Override
        public void run() {
          try {
            timeoutRec1(proximo_cliente);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }, TEMP_MAX);
      dispatchEventToClient(checkRecPrint, "COCA Liberou", proximo_cliente);
      return;
    }
    this.rec1Free = true;
    this.identifier1 = null;
    return;
  }

  // Liberar manualmente o recurso 2
  @CrossOrigin
  @PostMapping("/freerecPEPSI")
  public void freerec2(@RequestParam String procid) {
    // Verifica se e ele que possui o recurso
    if (!procid.equals(this.identifier2))
      return;

    dispatchEventToClient(freeRecPrint, "PEPSI Liberada", procid);

    // Se a fila nao estiver vazia, consede o proximo da fila
    if (fila2.size() > 0) {
      String proximo_cliente = fila2.get(0);
      this.identifier2 = proximo_cliente;
      fila2.remove(0);

      // Thread para liberar o recurso por timeout
      new Timer().schedule(new TimerTask() {
        @Override
        public void run() {
          try {
            timeoutRec2(proximo_cliente);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }, TEMP_MAX);
      dispatchEventToClient(checkRecPrint, "PEPSI Liberou", proximo_cliente);
      return;
    }
    this.rec2Free = true;
    this.identifier2 = null;
    return;
  }

  public void timeoutRec1(String procid) {
    // Testa se o cliente não liberou o recurso antes
    if (this.identifier1 != procid)
      return;

    // Libera o recurso
    if (this.identifier1 != null) {
      this.identifier1 = null;
      this.rec1Free = true;

      dispatchEventToClient(freeRecPrint, "Timeout COCA", procid);

      // Se a fila nao estiver vazia, consede o proximo da fila
      if (fila1.size() > 0) {
        this.rec1Free = false;
        String proximo_cliente = fila1.get(0);
        this.identifier1 = proximo_cliente;
        fila1.remove(0);

        dispatchEventToClient(checkRecPrint, "COCA Liberou", proximo_cliente);
        new Timer().schedule(new TimerTask() {
          @Override
          public void run() {
            try {
              timeoutRec1(proximo_cliente);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }, TEMP_MAX);
      }
    }
    return;
  }

  public void timeoutRec2(String procid) {
    // Testa se o cliente não liberou o recurso antes
    if (this.identifier2 != procid)
      return;

    // Libera o recurso
    if (this.identifier2 != null) {
      this.identifier2 = null;
      this.rec2Free = true;

      dispatchEventToClient(freeRecPrint, "Timeout PEPSI", procid);

      // Se a fila nao estiver vazia, consede o proximo da fila
      if (fila2.size() > 0) {
        this.rec2Free = false;
        String proximo_cliente = fila2.get(0);
        this.identifier2 = proximo_cliente;
        fila2.remove(0);

        dispatchEventToClient(checkRecPrint, "PEPSI Liberou", proximo_cliente);
        new Timer().schedule(new TimerTask() {
          @Override
          public void run() {
            try {
              timeoutRec2(proximo_cliente);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }, TEMP_MAX);
      }
    }
    return;
  }

  private void sendInitEvent(SseEmitter sseEmitter) {
    // Envia evento de inicialização
    try {
      sseEmitter.send(SseEmitter.event().name("INIT"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Método para a inscrição do cliente
  @CrossOrigin
  @RequestMapping(value = "/connect", consumes = MediaType.ALL_VALUE)
  public SseEmitter connect(@RequestParam String procid) {
    SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);

    System.out.println("Connection ID: " + procid);

    sendInitEvent(sseEmitter);

    emitters.put(procid, sseEmitter);
    sseEmitter.onCompletion(() -> {
      emitters.remove(procid);
    });
    sseEmitter.onTimeout(() -> {
      emitters.remove(procid);
    });
    sseEmitter.onError((e) -> {
      emitters.remove(procid);
    });

    return sseEmitter;
  }

  // Função que enviar eventos ao cliente
  public void dispatchEventToClient(String title, String text, String userId) {
    String eventFormatted = new JSONObject()
        .put("title", title)
        .put("text", text).toString();

    SseEmitter sseEmitter = emitters.get(userId);
    if (sseEmitter != null) {
      try {
        sseEmitter.send(SseEmitter.event().name("ServerEvent").data(eventFormatted));
      } catch (Exception e) {
        emitters.remove(userId);
      }
    }
  }
}