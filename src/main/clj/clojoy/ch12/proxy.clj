(ns clojoy.ch12.proxy
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [com.sun.net.httpserver HttpHandler HttpExchange HttpServer]
           [java.net InetSocketAddress URLDecoder URI]
           [java.io File FilterOutputStream]
           (java.nio.charset Charset)
           (clojure.lang IDeref)))

(def OK java.net.HttpURLConnection/HTTP_OK)

(defn respond
  ([^HttpExchange exchange ^String body]
   (respond identity exchange body))
  ([around ^HttpExchange exchange ^String body]
   (.sendResponseHeaders exchange OK 0)
   (with-open [resp (around (.getResponseBody exchange))]
     (.write resp (.getBytes body)))))

(defn new-server
  [port path handler]
  (doto
   (HttpServer/create (InetSocketAddress. port) 0)
    (.createContext path handler)
    (.setExecutor nil)
    (.start)))

;proxy可以生成一个类或接口的代理实例
(defn default-handler
  [txt]
  (proxy [HttpHandler]
         []
    (handle [exchange]
      (respond exchange txt))))

(def ^HttpServer server
  (new-server
   8123
   "/clojoy/hello"
   (default-handler "Hello,World")))

(.stop server 0)

(def p (default-handler
         "xxx"))
(def ^HttpServer server
  (new-server
   8123
   "/"
   p))

;update-proxy可以更新proxy,利用它可以做到热加载
(update-proxy
 p
 {"handle" (fn [this exchange]
             (respond exchange (str "this is " this)))})

(def echo-handler
  (fn [_ ^HttpExchange exchange]
    (let [headers (.getRequestHeaders exchange)]
      (respond exchange (prn-str headers)))))
(update-proxy p {"handle" echo-handler})

;获取一个proxy中的函数映射关系
(proxy-mappings p)

;proxy-super可以让我们调用父类的方法,类似super.xxx(),但它不是线程安全的
(defn html-around
  [o]
  (proxy [FilterOutputStream]
         [o]
    (write [^bytes raw-bytes]
      (proxy-super
       write
       (.getBytes (str "<html><body>"
                       (String. raw-bytes)
                       "</body></html>"))))))

(update-proxy
 p
 {"handle" (fn [_ exchange]
             (respond html-around exchange "Hello"))})

;实现一个网页的文件浏览器
(defn listing [file]
  (-> file .list sort))

(defn html-links [root filenames]
  (string/join
   (for [file filenames]
     (str "<a href='"
          (str root
               (if (= "/" root)
                 ""
                 File/separator)
               file)
          "'>"
          file "</a><br>"))))

(defn details [^File file]
  (str (.getName file) " is "
       (.length file) " bytes."))

(defn uri->file [root uri]
  (->> uri
       str
       URLDecoder/decode
       (str root)
       io/file))
(uri->file "." (URI. "/project.clj"))

(defn fs-handler
  [_ ^HttpExchange exchange]
  (let [uri (.getRequestURI exchange)
        file (uri->file "." uri)]
    (if (.isDirectory file)
      (do (.add (.getResponseHeaders exchange)
                "Content-Type" "text/html")
          (respond html-around exchange
                   (html-links (str uri) (listing file))))
      (respond exchange (details file)))))

(update-proxy p {"handle" fs-handler})

;proxy相关的其他一些函数
;get-proxy-class可以获取一个用于生成代理对象的类型
(def MyException (get-proxy-class Exception IDeref))
;construct-proxy可以构造一个代理类的实例,init-proxy可以初始化代理类的函数映射表
(defn bail
  ([ex s]
   (-> ex
       (construct-proxy s)
       (init-proxy
        {"deref" (fn [this] (str "Cause: " s))})))
  ([ex s e]
   (-> ex
       (construct-proxy s e)
       (init-proxy
        {"deref" (fn [this] (str "Root: " (.getMessage e)))}))))

