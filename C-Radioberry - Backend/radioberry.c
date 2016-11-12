/*
 * Radioberry 
 * 
 * Webserver + wdsp + frontend.
 *
 */

#include "mongoose.h"
#include "frozen.h"

static sig_atomic_t s_signal_received = 0;
static const char *s_http_port = "8000";
static struct mg_serve_http_opts s_http_server_opts;

static void signal_handler(int sig_num) {
  signal(sig_num, signal_handler);  // Reinstantiate signal handler
  s_signal_received = sig_num;
}

static int is_websocket(const struct mg_connection *nc) {
  return nc->flags & MG_F_IS_WEBSOCKET;
}

static void broadcast(struct mg_connection *nc, const struct mg_str msg) {
  struct mg_connection *c;
  char buf[500];
  char addr[32];
  mg_sock_addr_to_str(&nc->sa, addr, sizeof(addr),
                      MG_SOCK_STRINGIFY_IP | MG_SOCK_STRINGIFY_PORT);

  snprintf(buf, sizeof(buf), "%s %.*s", addr, (int) msg.len, msg.p);
  printf("%s\n", buf); /* Local echo. */
  for (c = mg_next(nc->mgr, NULL); c != NULL; c = mg_next(nc->mgr, c)) {
    if (c == nc) continue; /* Don't send to the sender. */
    mg_send_websocket_frame(c, WEBSOCKET_OP_TEXT, buf, strlen(buf));
  }
}

static void ev_handler(struct mg_connection *nc, int ev, void *ev_data) {
  switch (ev) {
	case MG_EV_HTTP_REQUEST: {
		mg_serve_http(nc, (struct http_message *) ev_data, s_http_server_opts);
		break;
	}
    case MG_EV_WEBSOCKET_HANDSHAKE_DONE: {
      /* New websocket connection. Tell everybody. */
      broadcast(nc, mg_mk_str("++ joined"));
      break;
    }
    case MG_EV_WEBSOCKET_FRAME: {
      struct websocket_message *wm = (struct websocket_message *) ev_data;
      /* New websocket message. Tell everybody. */
      struct mg_str d = {(char *) wm->data, wm->size};
      broadcast(nc, d);
      break;
    }
    case MG_EV_CLOSE: {
      /* Disconnect. Tell everybody. */
      if (is_websocket(nc)) {
        broadcast(nc, mg_mk_str("-- left"));
      }
      break;
    }
  }
}

static void handle_hello1(struct mg_connection *nc, int ev, void *ev_data) {
  
  /* Send headers */
  mg_printf(nc, "%s", "HTTP/1.1 200 OK\r\nTransfer-Encoding: chunked\r\n\r\n");

  /* Compute the result and send it back as a JSON object */
//  result = strtod(n1, NULL) + strtod(n2, NULL);
//  mg_printf_http_chunk(nc, "{ \"result\": %lf }", result);
//  mg_send_http_chunk(nc, "", 0); /* Send empty chunk, the end of response */
 
  mg_printf_http_chunk(nc, "%s", "my response!");
  mg_send_http_chunk(nc, "", 0); // Tell the client we're finished
	nc->flags |= MG_F_SEND_AND_CLOSE;
}


////"frequency":parseInt(1008000), "mode":parseInt(6), "agc":parseInt(0), "low":parseInt(-4000), "high":parseInt(4000), "agc_gain":parseFloat(85)};
static void handle_radiocontrol(struct mg_connection *nc, int ev, void *ev_data) {
 
	printf("radiocontrol url called \n");
	

	struct http_message *hm = (struct http_message *) ev_data;
	char buf[255] = {0};
	memcpy(buf, hm->body.p,
	   sizeof(buf) - 1 < hm->body.len ? sizeof(buf) - 1 : hm->body.len);
	printf("%s\n", buf);

	struct json_token tokens[10];
	int tokens_size = sizeof(tokens) / sizeof(tokens[0]);
	int i= parse_json(buf, strlen(buf), tokens, tokens_size);
	

	const struct json_token *result;
	result = find_json_token(tokens, "frequency");
	char freqToken[10];
	sprintf(freqToken, "%.*s", result->len, result->ptr);
	int freq;
	sscanf(freqToken, "%d", &freq);
	printf("Frequency %d\n", freq);

	printf("radiocontrol send \n");
	/* Send headers */
	mg_printf(nc, "%s", "HTTP/1.1 200 OK\r\nTransfer-Encoding: chunked\r\n\r\n");
	mg_send_http_chunk(nc, "", 0); // Tell the client we're finished
	nc->flags |= MG_F_SEND_AND_CLOSE;
}

int mainx(void) {
  struct mg_mgr mgr;
  struct mg_connection *nc;

  signal(SIGTERM, signal_handler);
  signal(SIGINT, signal_handler);
  setvbuf(stdout, NULL, _IOLBF, 0);
  setvbuf(stderr, NULL, _IOLBF, 0);

  mg_mgr_init(&mgr, NULL);

  nc = mg_bind(&mgr, s_http_port, ev_handler);
  s_http_server_opts.document_root = ".";
  mg_set_protocol_http_websocket(nc);
  
  mg_register_http_endpoint(nc, "/hello1", handle_hello1);
  mg_register_http_endpoint(nc, "/radioberry/control.do", handle_radiocontrol);
  

  printf("Started on port %s\n", s_http_port);
  while (s_signal_received == 0) {
    mg_mgr_poll(&mgr, 200);
  }
  mg_mgr_free(&mgr);

  return 0;
}
