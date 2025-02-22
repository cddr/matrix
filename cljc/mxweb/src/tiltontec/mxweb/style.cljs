(ns tiltontec.mxweb.style
  (:require
    [tiltontec.util.base :refer [type-cljc]]
    [tiltontec.util.core :refer [pln]]
    [tiltontec.cell.base :refer [md-ref? ia-type unbound]]
    [tiltontec.cell.observer :refer [observe observe-by-type]]
    [tiltontec.cell.evaluate :refer [not-to-be not-to-be-self]]
    [tiltontec.model.core
     :refer-macros [the-kids mdv!]
     :refer [mget fasc fm! make mset! backdoor-reset!]
     :as md]
    [tiltontec.mxweb.base :refer [tag? kw$]]
    [goog.dom.classlist :as classlist]
    [goog.style :as gstyle]
    [goog.dom :as dom]
    [cljs.pprint :as pp]
    [clojure.string :as str]))

;; todo move to utill or put all this in tiltontec.mxweb

(defn tag-dom [me]
  ;; This will return nil when 'me' is being awakened and rules
  ;; are firing for the first time, because 'me' has not yet
  ;; been installed in the actual DOM, so call this only
  ;; from event handlers and the like.
  (let [id (mget me :id)]
    (assert id)
    (or (:dom-cache @me) ;; todo this should be backdoor sth or in meta or sth
      (if-let [dom (dom/getElement (str id))]
        (backdoor-reset! me :dom-cache dom)
        (println :style-no-element id :found)))))

(defn make-css-inline [tag & stylings]
  (assert (tag? tag))
  #_ (prn :mkcss-sees tag (for [[k _] (partition 2 stylings)] k)
    stylings)
  (apply make
    :type :mxweb.css/css
    :tag tag
    :css-keys (for [[k _] (partition 2 stylings)] k)
    stylings))

(defn style-string [s]
  (let [ss (cond
             (string? s) s
             (nil? s) ""

             (map? s)
             (str/join ";"
               (for [[k v] s
                     :when v]
                 (pp/cl-format nil "~a:~a" (name k) (kw$ v))))

             (= :mxweb.css/css (ia-type s))
             (style-string (select-keys @s (:css-keys @s)))

             :default
             (do
               (pln :ss-unknown s (type s))
               ""))]
    ;; (pln :mxw-gens-ss ss)
    ss))

(defmethod observe-by-type [:mxweb.css/css] [slot me newv oldv _]
  (when (not= oldv unbound)
    (let [dom (tag-dom (:tag @me))]
      #_ (when (some #{slot} [:animation])
        (prn :CSS-obs-by-type slot newv oldv (kw$ newv)))
      (gstyle/setStyle dom (name slot) (kw$ newv)))))