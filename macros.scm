(define top-level-environment
  (lambda nil
    (current-environment)))

(define cadr
  (lambda x
    (car (cdr x))))

(define cddr
  (lambda x
    (cdr (cdr x))))

(define caddr
  (lambda x
    (car (cdr (cdr x)))))

(define reverse
  ((lambda reverse-helper
     (lambda x
	(reverse-helper (cons (list) x))))
  (lambda y
    (if (not (nil? (cdr y)))
      (reverse-helper (cons (cons (cadr y) (car y)) (cddr y)))
      (car y)))))

(define map
  ((lambda map-helper
     (lambda x
	(reverse (map-helper (cons (list) (cadr x))))))
  (lambda y
    (if (not (nil? (cdr y)))
      (map-helper (cons (cons (apply (car x) (list (cadr y)) (current-environment)) (car y)) (cddr y)))
      (car y)))))


(define macro-expand
  (lambda form
    (if (pair? form)
	    (;;(let ((expression (car form)))
	     (lambda keyword
		    ;;these are actually nested ifs like (if ? <then> (if ? <then> ...

		    ;;(quote val) => val
		    (if (eqv? keyword (string->symbol "quote"))
		      (cadr form)

		    ;;(let (var val) body) => ((lambda var body) val)
		    (if (eqv? keyword (string->symbol "let"))
		      (;;(let ((assignment-list (cadr form))
		       (lambda assignment-list
			 (;;(let ((body (caddr form)))
			  (lambda body
			    (map (list macro-expand
				       (list (list (string->symbol "lambda") (car assignment-list) body) (cadr assignment-list)))))
			 (caddr form)))
		       (cadr form))

		    (if (eqv? keyword (string->symbol "define"))
		      ((lambda name
			 ((lambda value
			    (eval (macro-expand (list (list (string->symbol "lambda") name (read (current-input-port))) value)) (top-level-environment)))
			  (caddr form)))
		       (cadr form))



		      (map (list macro-expand form)))))

		    )

	     (car form))

	    form)))

(define repl
  (lambda nil
	  ((lambda nil
	     (repl))
	     (write (eval (macro-expand (read (current-input-port))) (top-level-environment)) (current-output-port)))))



