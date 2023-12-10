(ns zk.model
  (:require [clojure.spec.alpha :as s] [clojure.string :as str])
  (:gen-class))

; https://joplinapp.org/help/api/references/rest_api#properties
; https://github.com/laurent22/joplin/blob/069c7a6b34510cbba041f6fe0cf00ca41e79b7fe/packages/lib/services/rest/routes/notes.ts#L58

; NOTES
(s/def :jop/id string?)
(s/def :jop/parent_id string?) ; ID of the notebook that contains this note. Change this ID to move the note to a different notebook.
(s/def :jop/title string?)
(s/def :jop/body string?) ; The note body, in Markdown. May also contain HTML.
(s/def :jop/created_time integer?) ; When the note was created.
(s/def :jop/updated_time integer?) ; When the note was last updated.
(s/def :jop/is_conflict string?) ; Tells whether the note is a conflict or not.
(s/def :jop/latitude number?)
(s/def :jop/longitude number?)
(s/def :jop/altitude number?)
(s/def :jop/author string?)
(s/def :jop/source_url string?) ; The full URL where the note comes from.
(s/def :jop/is_todo integer?) ; Tells whether this note is a todo or not.
(s/def :jop/todo_due integer?) ; When the todo is due. An alarm will be triggered on that date.
(s/def :jop/todo_completed integer?) ; Tells whether todo is completed or not. This is a timestamp in milliseconds.
(s/def :jop/source string?)
(s/def :jop/source_application string?)
(s/def :jop/application_data string?)
(s/def :jop/order number?)
(s/def :jop/user_created_time integer?) ; When the note was created. It may differ from created_time as it can be manually set by the user.
(s/def :jop/user_updated_time integer?) ; When the note was last updated. It may differ from updated_time as it can be manually set by the user.
(s/def :jop/encryption_cipher_text string?)
(s/def :jop/encryption_applied integer?)
(s/def :jop/markup_language integer?)
(s/def :jop/is_shared integer?)
(s/def :jop/share_id string?)
(s/def :jop/conflict_original_id string?)
(s/def :jop/master_key_id string?)
(s/def :jop/user_data string?)
(s/def :jop/body_html string?) ; Note body, in HTML format
(s/def :jop/base_url string?) ; If body_html is provided and contains relative URLs, provide the base_url parameter too so that all the URLs can be converted to absolute ones. The base URL is basically where the HTML was fetched from, minus the query (everything after the '?'). For example if the original page was https://stackoverflow.com/search?q=%5Bjava%5D+test, the base URL is https://stackoverflow.com/search.
(s/def :jop/image_data_url string?) ; An image to attach to the note, in Data URL format.
(s/def :jop/crop_rect string?) ; If an image is provided, you can also specify an optional rectangle that will be used to crop the image. In format { x: x, y: y, width: width, height: height }


(s/def :jop/items (s/coll-of map?))
(s/def :jop/has_more boolean?)
(s/def :jop/response-items 
  (s/keys :req-un [:jop/items :jop/has_more]) )


(def response-example
  {:items [{:id "2873917c86b44a55af4feec8b0024e53",
            :parent_id "02d6c6e4fdda4f49bdd3a2291ba6d303"}]
   :has_more false})

;(s/explain ::response-items response-example)

(def item-types
  {:jop/note 1
   :jop/folder 2
   :jop/setting 3
   :jop/resource 4
   :jop/tag 5
   :jop/note_tag 6
   :jop/search 7
   :jop/alarm 8
   :jop/master_key 9
   :jop/item_change 10
   :jop/note_resource 11
   :jop/resource_local_state 12
   :jop/revision 13
   :jop/migration 14
   :jop/smart_filter 15
   :jop/command 16})


(s/def ::note
  (s/keys :req-un [::id]
          :opt-un [:jop/altitude :jop/application_data :jop/author :jop/body 
                   :jop/conflict_original_id :jop/created_time :jop/encryption_applied
                   :jop/encryption_cipher_text :jop/is_conflict :jop/is_shared
                   :jop/is_todo ::latitude :jop/longitude :jop/markup_language :jop/master_key_id
                   :jop/order :jop/parent_id :jop/share_id :jop/source :jop/source_application
                   :jop/source_url :jop/title :jop/todo_completed :jop/todo_due :jop/updated_time
                   :jop/user_created_time :jop/user_data :jop/user_updated_time

                   ; STATUS 500
                   ;::crop_rect ::body_html ::base_url ::image_data_url 
                   ]))

(s/def ::tag
  (s/keys :req-un [::id]
          :opt-un [:jop/title :jop/created_time :jop/updated_time
                   :jop/user_created_time :jop/user_updated_time
                   :jop/encryption_applied :jop/is_shared :jop/parent_id :jop/user_data
                   
                   ; STATUS 500
                   ; ::encryption_cipher
                   ]))

(defn successor 
  "successor of first occurence of `item` in `coll` or `nil`"
  [item coll]
  (let [ix (.indexOf coll item)]
    ( if (= ix -1) nil (coll (inc ix)))))

(defn spec-keys
  "get  keys defined in :req :req-un :opt :opt-un in one list"
  [spec-symbol]
  (let [spec-form (vec (s/form (s/get-spec spec-symbol)))]
   (flatten (filter some? (map #(successor % spec-form) [:req :req-un :opt :opt-un])))))


(defn list-specs 
  "List all specs for given namespace. Replace namespace with prefix."
  ([ns prefix]
   (map #(str prefix %)
        (sort (map name (filter #(str/starts-with? % ns) (keys (s/registry)))))))
  ([] (list-specs ":zk.model" "::")))


(defmacro spec-group-keys
  "Defines a new spec based on existing with all keys moved in one group (:req :req-un :opt :opt-un)"
  [old-spec group new-spec]
  (list 's/def new-spec (list 's/keys group (vec (spec-keys old-spec)))))


;  (s/describe (spec-group-keys ::tag :req-un ::temp))