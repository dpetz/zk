(ns zk.model
  (:require [clojure.spec.alpha :as s] [clojure.string :as str])
  (:gen-class))

; https://joplinapp.org/help/api/references/rest_api#properties


; NOTES

(s/def ::id string?)
(s/def ::parent_id string?) ; ID of the notebook that contains this note. Change this ID to move the note to a different notebook.
(s/def ::title string?)
(s/def ::body string?) ; The note body, in Markdown. May also contain HTML.
(s/def ::created_time integer?) ; When the note was created.
(s/def ::updated_time integer?) ; When the note was last updated.
(s/def ::is_conflict string?) ; Tells whether the note is a conflict or not.
(s/def ::latitude number?)
(s/def ::longitude number?)
(s/def ::altitude number?)
(s/def ::author string?)
(s/def ::source_url string?) ; The full URL where the note comes from.
(s/def ::is_todo integer?) ; Tells whether this note is a todo or not.
(s/def ::todo_due integer?) ; When the todo is due. An alarm will be triggered on that date.
(s/def ::todo_completed integer?) ; Tells whether todo is completed or not. This is a timestamp in milliseconds.
(s/def ::source string?)
(s/def ::source_application string?)
(s/def ::application_data string?)
(s/def ::order number?)
(s/def ::user_created_time integer?) ; When the note was created. It may differ from created_time as it can be manually set by the user.
(s/def ::user_updated_time integer?) ; When the note was last updated. It may differ from updated_time as it can be manually set by the user.
(s/def ::encryption_cipher_text string?)
(s/def ::encryption_applied integer?)
(s/def ::markup_language integer?)
(s/def ::is_shared integer?)
(s/def ::share_id string?)
(s/def ::conflict_original_id string?)
(s/def ::master_key_id string?)
(s/def ::user_data string?)
(s/def ::body_html string?) ; Note body, in HTML format
(s/def ::base_url string?) ; If body_html is provided and contains relative URLs, provide the base_url parameter too so that all the URLs can be converted to absolute ones. The base URL is basically where the HTML was fetched from, minus the query (everything after the '?'). For example if the original page was https://stackoverflow.com/search?q=%5Bjava%5D+test, the base URL is https://stackoverflow.com/search.
(s/def ::image_data_url string?) ; An image to attach to the note, in Data URL format.
(s/def ::crop_rect string?) ; If an image is provided, you can also specify an optional rectangle that will be used to crop the image. In format { x: x, y: y, width: width, height: height }

; TAGS (additional)

(s/def ::note
  (s/keys :req-un [::id]
          :opt-un [::altitude ::application_data ::author ::base_url ::body ::body_html
                   ::conflict_original_id ::created_time ::crop_rect ::encryption_applied
                   ::encryption_cipher_text ::image_data_url ::is_conflict ::is_shared
                   ::is_todo ::latitude ::longitude ::markup_language ::master_key_id
                   ::order ::parent_id ::share_id ::source ::source_application
                   ::source_url ::title ::todo_completed ::todo_due ::updated_time
                   ::user_created_time ::user_data ::user_updated_time]))

(s/def ::tag
  (s/keys :req-un [::id]
          :opt-un [::title ::created_time ::updated_time ::user_created_time ::user_updated_time
                   ::encryption_applied ::is_shared ::parent_id ::user_data
                  ; ::encryption_cipher --> error HTTP status: 500
                   ]))

(defn successor 
  "successor of first occurence of `item` in `coll` or `nil`"
  [item coll]
  (let [ix (.indexOf coll item)]
    ( if (= ix -1) nil (coll (inc ix)))))

(defn spec-keys
  [spec-symbol]
  (let [spec-form (vec (s/form (s/get-spec spec-symbol)))]
   (flatten (filter some? (map #(successor % spec-form) [:req :req-un :opt :opt-un])))))


(defn list-specs
  ([prefix]
   (map #(str prefix %)
        (sort (map name (filter #(str/starts-with? % ":zk.model") (keys (s/registry)))))))
  ([] (list-specs "::")))


(defmacro spec-group-keys
  [old-spec group new-spec]
  (list 's/def new-spec (list 's/keys group (vec (spec-keys old-spec)))))


;  (s/describe (spec-group-keys ::tag :req-un ::temp))