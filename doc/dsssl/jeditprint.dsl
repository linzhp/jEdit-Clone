<!DOCTYPE style-sheet PUBLIC "-//James Clark//DTD DSSSL Style Sheet//EN" [
<!ENTITY dbstyle SYSTEM "/usr/local/sgmltools-2.0.2/share/sgml/stylesheets/sgmltools/print.dsl" CDATA DSSSL>
]>

<style-sheet>
<style-specification use="print">
<style-specification-body>

(define %two-side% 
  #t)
(define %left-margin%
  4pi)
(define %top-margin%
  4pi)
(define %right-margin%
  4pi)
(define %bottom-margin%
  4pi)

(define %admon-graphics% #f)

;; Since we're producing PDF output, we need to use PNG images
(define %graphic-default-extension% "png")

;; DocBook should have some sort of %img-dir% variable, but for now,
;; a stupid hack

(define (graphic-file filename)
   (let ((ext (file-extension filename)))
      (if (or (not filename)
              (not %graphic-default-extension%)
	      (member ext %graphic-extensions%))
	  filename
	  (string-append "images/" filename "." %graphic-default-extension%))))

</style-specification-body>
</style-specification>
<external-specification id="print" document="dbstyle">
</style-sheet>
