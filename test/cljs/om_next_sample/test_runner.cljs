(ns om-next-sample.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [om-next-sample.core-test]
   [om-next-sample.common-test]))

(enable-console-print!)

(doo-tests 'om-next-sample.core-test
           'om-next-sample.common-test)
