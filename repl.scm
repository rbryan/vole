(load "macros.scm")

(define eval-loop 
  (lambda () 
	  ((lambda ()
	     (eval-loop))
	     (eval (macro-expand (read (current-input-port))) (top-level-environment)))))
