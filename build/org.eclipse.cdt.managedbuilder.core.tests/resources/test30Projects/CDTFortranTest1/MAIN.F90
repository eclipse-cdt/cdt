  PROGRAM MAIN
    USE AVE_CALCULATOR    
    INTEGER :: AVE
    INTEGER, DIMENSION(3) :: ENTERED = 0
    PRINT *, 'Type three integers: '
    READ  (*,'(I10)') ENTERED
    AVE = AVERAGE(ENTERED)
    PRINT *, 'Answer is: ', AVE
  END PROGRAM MAIN
