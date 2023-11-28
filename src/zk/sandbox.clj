(ns zk.sandbox
  (:import 
   (com.vladsch.flexmark.util.data MutableDataSet)
   (com.vladsch.flexmark.parser Parser)
   ; flexmark-ext-definition
   (com.vladsch.flexmark.ext.definition DefinitionExtension)
   )
  (:require
   [cybermonday.core] [cybermonday.ir] [cybermonday.parser])
  )


 
; https://github.com/kiranshila/cybermonday/blob/master/src/cybermonday/parser.clj

 (def tmp
   (.. (MutableDataSet. cybermonday.parser/options)
       (set Parser/EXTENSIONS
            [(DefinitionExtension/create)])
       (toImmutable)))


(defn parse-math [[_ _ & [math]]]
   (str "$$" math "$$"))

(cybermond
 ay.core/parse-md "$`y=mx+b`$")

; (cybermonday.core/parse-md "$`y=mx+b`$" {:markdown/inline-math parse-math})




