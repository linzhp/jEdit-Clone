/* Filename: jEdit.cmd                                                     */
/* Description : Simple REXX script to execute Slava Pestov's jEdit, a     */
/*               free Java Editor under OS/2 with JDK 1.1.4 or higher      */
/* Installation: Just place this file in the jEdit directory (this is the  */
/*               directory which contains the following sub-directories:   */
/*                    /doc                                               ) */
/*                    /org                                                 */
/*                    ...                                                  */
/*                                                                         */
/*                                                                         */
/*                                        (w) by  Alexander Hoff           */
/*                                                N.E.W.S. GbR             */
/*                                                Herrenhof 13             */
/*                                                41515 Grevenbroich       */
/*                                                Germany                  */
/*                                                Email: AHoff@news-gbr.de */
/*                                                Tel: +49-2181-2426-30    */
/*                                                                         */

      /* load  REXX functions */
if RxFuncQuery('SysLoadFuncs') then do
 call RxFuncAdd 'SysLoadFuncs','REXXUTIL','SysLoadFuncs'
 call SysLoadFuncs 
end

parse arg files

setlocal

   /* check, if the tmp environment variable is set */
tmppath=GetValue('TMP')
if tmppath='' then do
  say ''
  say '_______________________________________________________'
  say 'TMP environment variable not set!'
  say ' Please set TMP in your config.sys to point to your'
  say ' TMP directory.'
  say '  For Example: SET TMP=D:\TMP\BAK'
  say ''
  say '  Alternative you can set the environment variable now'
  say '  manually. This would be a non permanent change.'
  say '  Do you want to do this (Y/N)?'
  say '_______________________________________________________'
  say ''
  key=KeyPressed()
  if (key='Y') then do
     say '  Please enter the desired tmp directory'
     say '     For Example: C:\TMP  (<ENTER> to end):'
     NewDir = strip( lineIn() )
     if (DirExists(NewDir)<>0) then do
       say ''
       say '  The directory you specified does not exists. Do you want me to '
       say '  to create it (Y/N)?'
       key=KeyPressed()
       if (key='Y') then 'mkdir 'NewDir''
       else do
          endlocal
          exit
       end /* of else */
     end /* of if (DirExists ... */
  end /* of if (key= ... */
  else do
    endlocal
    exit
  end /* of else */
end /* of if (tmppath= ... */

    /* update tmppath variable. it may have changed  */
tmppath=GetValue('TMP')

   /* get current drive and directory */
parse upper source . . sourcefile

prgdrive = filespec('DRIVE', sourcefile)
prghelp = filespec('PATH', sourcefile)
prgpath = prgdrive''prghelp

   /* setup environment variables */
'set classpath='prgpath'jedit.jar;%CLASSPATH%'

     /* start the program */
'java org.gjt.sp.jedit.jEdit 'files''

endlocal

exit


/*---------------------------------------------------------------------*/
/* Subroutines                                                         */
/*---------------------------------------------------------------------*/

GetValue: Procedure             /* Obtain an environment variable      */
  Arg EnvVal .
  res  = value(EnvVal,,'OS2ENVIRONMENT')
Return res

/*---------------------------------------------------------------------*/
KeyPressed: Procedure
   key=SysGetKey(NOECHO)
   key= TRANSLATE(key)
Return key

/*---------------------------------------------------------------------*/
DirExists: Procedure
  parse arg testdir .

                         /* init the return code                       */
   thisRC = ""
 
                         /* install a temporary error handler to check */
                         /* if the drive with the directory to test is */
                         /* ready                                      */
   SIGNAL ON NOTREADY NAME DirDoesNotExist
 
                         /* check if the drive is ready                */
   call stream testDir || "\*", "D"
 
 
                         /* save the current directory of the current  */
                         /* drive                                      */
   curDir = directory()
 
                         /* save the current directory of the drive    */
                         /* with the directory to test                 */
   curDir1 = directory( fileSpec( "drive", testDir ) )
 
 
                         /* test if the directory exist                */
   thisRC = directory( testDir )
 
                         /* restore the current directory of the drive */
                         /* with the directory to test                 */
   call directory curDir1
 
                         /* restore the current directory of the       */
                         /* current drive                              */
   call directory curDir

   if (thisRC=testDir) then thisRC=0
   else thisRC=1

   Return thisRC

DirDoesNotExist:
 
Return 2
 
/*---------------------------------------------------------------------*/   
