(ns zk.joplin
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [clojure.spec.alpha :as s]
            [zk.model :as model]))
 


; https://joplinapp.org/help/api/references/rest_api#properties
; https://github.com/laurent22/joplin/blob/069c7a6b34510cbba041f6fe0cf00ca41e79b7fe/packages/lib/services/rest/routes/notes.ts#L58

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


(s/def ::items (s/coll-of map?))
(s/def ::has_more boolean?)
(s/def ::response-items 
  (s/keys :req-un [::items ::has_more]) )


(def response-example
  {:items [{:id "2873917c86b44a55af4feec8b0024e53",
            :parent_id "02d6c6e4fdda4f49bdd3a2291ba6d303"}]
   :has_more false})

;(s/explain ::response-items response-example)

(def item-types
  {:note 1
   :folder 2
   :setting 3
   :resource 4
   :tag 5
   :note_tag 6
   :search 7
   :alarm 8
   :master_key 9
   :item_change 10
   :note_resource 11
   :resource_local_state 12
   :revision 13
   :migration 14
   :smart_filter 15
   :command 16})


(s/def ::note
  (s/keys :req-un [::id]
          :opt-un [::altitude ::application_data ::author ::body 
                   ::conflict_original_id ::created_time ::encryption_applied
                   ::encryption_cipher_text ::is_conflict ::is_shared
                   ::is_todo ::latitude ::longitude ::markup_language ::master_key_id
                   ::order ::parent_id ::share_id ::source ::source_application
                   ::source_url ::title ::todo_completed ::todo_due ::updated_time
                   ::user_created_time ::user_data ::user_updated_time

                   ; STATUS 500
                   ;::crop_rect ::body_html ::base_url ::image_data_url 
                   ]))

(s/def ::tag
  (s/keys :req-un [::id]
          :opt-un [::title ::created_time ::updated_time ::user_created_time ::user_updated_time
                   ::encryption_applied ::is_shared ::parent_id ::user_data
                   
                   ; STATUS 500
                   ; ::encryption_cipher
                   ]))


; https://joplinapp.org/help/api/references/rest_api/
(def port "http://localhost:41184/")
(def token "dcd2cbd0584a6c9d3b494c2ae78b0fc56255c1134b2ab8da8ffabbe78a7dcb28bda4f29360789d2c98d295283147af3f1515538986009fbdb143041de2403a95")

(defn default-params
  [overwrites]
  (merge {:page 1
          :limit 100
          :order_by "updated_time"
          :order_dir " DESC"}
         overwrites))

(defn only
  "if collection has single item returns it, otherwise nil"
  [col] (case (count col) 1 (first col) nil))

(defn if-single-key-drop-it
  "E.g. [{:title \"youtube\"} ..] to [\"youtube\" ..]. Otherwise unchanged."
  [coll]
  (if-let [key (only (keys (coll 0)))]
    (map #(% key) coll)
    coll))


(defn syms-to-str [col] (str/join "," (map name col)))

(def ALL 99999)

(defn unwrap-if-alone
  [coll]
  (if (empty? (rest coll)) (first coll) coll))

(def INFO false)

(defn auth-get
  "If single field, returns collection of values. Empty fields vector for standard fields"
  ([verb fields params]
   {:pre [(coll? fields) (map? params)]}

   (let [params-all (default-params (assoc params :token token :fields (syms-to-str fields)))]

     (loop [cache [] 
            pars (update params-all :limit #(min % 100))
            gap (params-all :limit)]
       (let [response ; save-request? true :debug true 
             (http/get (str port verb) {:accept :json :query-params (walk/stringify-keys pars)})

             body
             (walk/keywordize-keys  (json/read-str (:body response))) ; 

             {:keys [items has_more]}
             (if (s/valid? ::response-items body) body {:items (vector body) :has_more false})

             cached
             (concat cache (take gap (if-single-key-drop-it items)))

             gap-new
             (- gap (count items))]

         (when INFO (println (str/upper-case verb) body))
         (if (and has_more (pos? gap-new))
           (recur cached (update pars :page #(inc %)) gap-new)
           (unwrap-if-alone  cached))))))

  ([verb fields]
   (auth-get verb fields {})))



; ========== NOTES ==========

(def note-keys
  (model/spec-keys ::note))

(defn notes
  ([fields params]
   (auth-get "notes" fields params))
  ([fields] (notes fields {}))
  ([] (notes note-keys)))

(defn note [id]
  (auth-get (str "notes/" id) note-keys)) 

; ========== TAGS ==========

(def tag-keys
  (model/spec-keys ::tag))

(defn tags
  ([fields params]
   (auth-get "tags" fields params))
  ([] (tags tag-keys {})))

; ========== SEARCH ==========

; https://joplinapp.org/help/#searching
; GET /search?query=recipes&type=folder
(defn search
  ([query type fields params]
   {:pre [(contains? item-types type)]}
   (auth-get "search" fields (merge {:query query :type (name type)} params )))
  ([query type fields]
   (search query type fields {}))
  ([query type]
   (search query type []))
  ([query]
   (search query :note)))

