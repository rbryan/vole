(toggle-debug)

(define cadr
  (lambda x
    (car (cdr x))))

(define cddr
  (lambda x
    (cdr (cdr x))))

(define caddr
  (lambda x
    (car (cdr (cdr x)))))

(define begin
  (lambda x
    (if (not (nil? (cdr x)))
      (begin (cdr x))
      (car x))))

(define apply
  (lambda x
	  (eval (cons (car x) (cadr x)) (current-environment))))

(define assert
  (lambda x
    (if x
      #t
      (error "failed assertion"))))

(define macro-expand
  (lambda form
    (if (pair? form)
	    (;;(let ((expression (car form)))
	     (lambda keyword
		(begin
		  (list

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
			    (list (list (string->symbol "lambda") (car assignment-list) body) (cadr assignment-list)))
			 (caddr form)))
		       (cadr form))



		      #f))

		    )))

	     (car form))

	    form)))

