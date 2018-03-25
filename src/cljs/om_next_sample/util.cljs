(ns om-next-sample.util
  (:require [cognitect.transit :as t]
            [taoensso.timbre :refer-macros [log trace debug info warn error fatal report]])
  (:import [goog.net XhrIo EventType]
           [goog.events.listen]))

(defn transit-post [url]
  (fn [edn callback-fn]
    (info "#### send-fn ####" edn)
    (.send XhrIo url
      (fn [e]
        (this-as this
          (try
            (let [res (.getResponseText this)
                  parsed (t/read (t/reader :json) res)]
              (try
                (callback-fn parsed)
                (catch js/Error ex
                  (error "Exception occurred in transit-post callback." ex)
                  (error "parsed edn=" parsed)
                  (error "stacktrace=" ex.stack))))
            (catch js/Error ex
              (error "Exception occurred in parse transit response." ex)
              (error "responseText=" (.getResponseText this))
              (error "stacktrace=" ex.stack)))))
      "POST" (t/write (t/writer :json) edn)
      #js {"Content-Type" "application/transit+json"})))

(defn download-file
  [url & {:keys [on-success on-error]}]
  (let [xhrio (XhrIo.)]
    (.setResponseType xhrio XhrIo.ResponseType.ARRAY_BUFFER)
    (goog.events.listen xhrio EventType.SUCCESS on-success)
    (goog.events.listen xhrio EventType.ERROR   on-error)
    (.send xhrio url "GET" nil #js {"Content-type" "application/octet-stream"})))

;; (def ext-re->mime-type
;;   {#"(?i)\.(sql|txt)$" "text/plain"
;;    #"(?i)\.pdf$" "application/pdf"
;;    #"(?i)\.jpg$" "image/jpg"
;;    #"(?i)\.png$" "image/png"
;;    #"(?i)\.bmp$" "image/bmp"
;;    #"(?i)\.xls$" "application/vnd.ms-excel"
;;    #"(?i)\.doc$" "application/ms-word"
;;    #"(?i)\.ppt$" "application/vnd.ms-powerpoint"
;;    #"(?i)\.(xlsx|xlsm)$" "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
;;    #"(?i)\.docx$"        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
;;    #"(?i)\.pptx$"        "application/vnd.openxmlformats-officedocument.presentationml.presentation"})

;; (defn ext->mime-type
;;   "lookup mime type from file extension."
;;   [filename]
;;   (let [ks (keys ext-re->mime-type)]
;;     nil))
