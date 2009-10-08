################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
F90_SRCS += \
../module/F_module.f90 \
../module/a1_module.f90 \
../module/a2_module.f90 \
../module/a_module.f90 \
../module/abc_module.f90 \
../module/b1_module.f90 \
../module/b2_module.f90 \
../module/c1_module.f90 \
../module/c2_module.f90 \
../module/c3_module.f90 \
../module/cba_module.f90 \
../module/cr_module.f90 \
../module/d.f90 \
../module/e_module.f90 \
../module/et_module.f90 \
../module/g_module.f90 \
../module/jj_module.f90 \
../module/k_module.f90 \
../module/l1_module.f90 \
../module/l_module.f90 \
../module/m1_module.f90 \
../module/mf_module.f90 \
../module/mg_module.f90 \
../module/p_module.f90 \
../module/un_module.f90 \
../module/v_module.f90 \
../module/w_module.f90 \
../module/xxx_module.f90 \
../module/xyz_module.f90 \
../module/yyy_module.f90 

OBJS += \
./module/F_module.obj \
./module/a1_module.obj \
./module/a2_module.obj \
./module/a_module.obj \
./module/abc_module.obj \
./module/b1_module.obj \
./module/b2_module.obj \
./module/c1_module.obj \
./module/c2_module.obj \
./module/c3_module.obj \
./module/cba_module.obj \
./module/cr_module.obj \
./module/d.obj \
./module/e_module.obj \
./module/et_module.obj \
./module/g_module.obj \
./module/jj_module.obj \
./module/k_module.obj \
./module/l1_module.obj \
./module/l_module.obj \
./module/m1_module.obj \
./module/mf_module.obj \
./module/mg_module.obj \
./module/p_module.obj \
./module/un_module.obj \
./module/v_module.obj \
./module/w_module.obj \
./module/xxx_module.obj \
./module/xyz_module.obj \
./module/yyy_module.obj 


# Each subdirectory must supply rules for building sources it contributes
module/%.obj: ../module/%.f90
	@echo 'Building file: $<'
	@echo 'Invoking: Test Fortran Compiler'
	myfort  -c -object:"$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '

module/F_module.obj: C3_MODULE.mod ../module/c3_module.f90 CBA_MODULE.mod ../module/cba_module.f90 D.mod ../module/d.f90 JJ_MODULE.mod ../module/jj_module.f90
./F_MODULE.mod: module/F_module.obj C3_MODULE.mod ../module/c3_module.f90 CBA_MODULE.mod ../module/cba_module.f90 D.mod ../module/d.f90 JJ_MODULE.mod ../module/jj_module.f90

module/a1_module.obj: B1_MODULE.mod ../module/b1_module.f90 CBA_MODULE.mod ../module/cba_module.f90 e_MODULE.mod ../module/e_module.f90 JJ_MODULE.mod ../module/jj_module.f90 K_MODULE.mod ../module/k_module.f90
./A1_MODULE.mod: module/a1_module.obj B1_MODULE.mod ../module/b1_module.f90 CBA_MODULE.mod ../module/cba_module.f90 e_MODULE.mod ../module/e_module.f90 JJ_MODULE.mod ../module/jj_module.f90 K_MODULE.mod ../module/k_module.f90

module/a2_module.obj: D.mod ../module/d.f90
./A2_MODULE.mod: module/a2_module.obj D.mod ../module/d.f90

module/a_module.obj: D.mod ../module/d.f90
./A_MODULE.mod: module/a_module.obj D.mod ../module/d.f90

./ABC_MODULE.mod: module/abc_module.obj

module/b1_module.obj: A2_MODULE.mod ../module/a2_module.f90
./B1_MODULE.mod: module/b1_module.obj A2_MODULE.mod ../module/a2_module.f90

module/b2_module.obj: CR_MODULE.mod ../module/cr_module.f90 UN_MODULE.mod ../module/un_module.f90
./B2_MODULE.mod: module/b2_module.obj CR_MODULE.mod ../module/cr_module.f90 UN_MODULE.mod ../module/un_module.f90

module/c1_module.obj: B1_MODULE.mod ../module/b1_module.f90 JJ_MODULE.mod ../module/jj_module.f90
./C1_MODULE.mod: module/c1_module.obj B1_MODULE.mod ../module/b1_module.f90 JJ_MODULE.mod ../module/jj_module.f90

module/c2_module.obj: A_MODULE.mod ../module/a_module.f90 D.mod ../module/d.f90 K_MODULE.mod ../module/k_module.f90 P_MODULE.mod ../module/p_module.f90
./C2_MODULE.mod: module/c2_module.obj A_MODULE.mod ../module/a_module.f90 D.mod ../module/d.f90 K_MODULE.mod ../module/k_module.f90 P_MODULE.mod ../module/p_module.f90

module/c3_module.obj: A2_MODULE.mod ../module/a2_module.f90 CBA_MODULE.mod ../module/cba_module.f90 D.mod ../module/d.f90 JJ_MODULE.mod ../module/jj_module.f90 L1_MODULE.mod ../module/l1_module.f90 MF_MODULE.mod ../module/mf_module.f90 V_MODULE.mod ../module/v_module.f90 YYY_MODULE.mod ../module/yyy_module.f90
./C3_MODULE.mod: module/c3_module.obj A2_MODULE.mod ../module/a2_module.f90 CBA_MODULE.mod ../module/cba_module.f90 D.mod ../module/d.f90 JJ_MODULE.mod ../module/jj_module.f90 L1_MODULE.mod ../module/l1_module.f90 MF_MODULE.mod ../module/mf_module.f90 V_MODULE.mod ../module/v_module.f90 YYY_MODULE.mod ../module/yyy_module.f90

module/cba_module.obj: A2_MODULE.mod ../module/a2_module.f90 A_MODULE.mod ../module/a_module.f90 CR_MODULE.mod ../module/cr_module.f90 JJ_MODULE.mod ../module/jj_module.f90 K_MODULE.mod ../module/k_module.f90 L1_MODULE.mod ../module/l1_module.f90 MF_MODULE.mod ../module/mf_module.f90 UN_MODULE.mod ../module/un_module.f90 XXX_MODULE.mod ../module/xxx_module.f90 XYZ_MODULE.mod ../module/xyz_module.f90 YYY_MODULE.mod ../module/yyy_module.f90
./CBA_MODULE.mod: module/cba_module.obj A2_MODULE.mod ../module/a2_module.f90 A_MODULE.mod ../module/a_module.f90 CR_MODULE.mod ../module/cr_module.f90 JJ_MODULE.mod ../module/jj_module.f90 K_MODULE.mod ../module/k_module.f90 L1_MODULE.mod ../module/l1_module.f90 MF_MODULE.mod ../module/mf_module.f90 UN_MODULE.mod ../module/un_module.f90 XXX_MODULE.mod ../module/xxx_module.f90 XYZ_MODULE.mod ../module/xyz_module.f90 YYY_MODULE.mod ../module/yyy_module.f90

module/cr_module.obj: UN_MODULE.mod ../module/un_module.f90
./CR_MODULE.mod: module/cr_module.obj UN_MODULE.mod ../module/un_module.f90

./D.mod: module/d.obj

module/e_module.obj: ABC_MODULE.mod ../module/abc_module.f90 ET_MODULE.mod ../module/et_module.f90
./e_MODULE.mod: module/e_module.obj ABC_MODULE.mod ../module/abc_module.f90 ET_MODULE.mod ../module/et_module.f90

module/et_module.obj: A_MODULE.mod ../module/a_module.f90 ABC_MODULE.mod ../module/abc_module.f90 CBA_MODULE.mod ../module/cba_module.f90 D.mod ../module/d.f90 JJ_MODULE.mod ../module/jj_module.f90 K_MODULE.mod ../module/k_module.f90 MF_MODULE.mod ../module/mf_module.f90 UN_MODULE.mod ../module/un_module.f90 XXX_MODULE.mod ../module/xxx_module.f90 XYZ_MODULE.mod ../module/xyz_module.f90 YYY_MODULE.mod ../module/yyy_module.f90
./ET_MODULE.mod: module/et_module.obj A_MODULE.mod ../module/a_module.f90 ABC_MODULE.mod ../module/abc_module.f90 CBA_MODULE.mod ../module/cba_module.f90 D.mod ../module/d.f90 JJ_MODULE.mod ../module/jj_module.f90 K_MODULE.mod ../module/k_module.f90 MF_MODULE.mod ../module/mf_module.f90 UN_MODULE.mod ../module/un_module.f90 XXX_MODULE.mod ../module/xxx_module.f90 XYZ_MODULE.mod ../module/xyz_module.f90 YYY_MODULE.mod ../module/yyy_module.f90

module/g_module.obj: B1_MODULE.mod ../module/b1_module.f90 CBA_MODULE.mod ../module/cba_module.f90 JJ_MODULE.mod ../module/jj_module.f90 L_MODULE.mod ../module/l_module.f90 W_MODULE.mod ../module/w_module.f90
./G_MOD.mod: module/g_module.obj B1_MODULE.mod ../module/b1_module.f90 CBA_MODULE.mod ../module/cba_module.f90 JJ_MODULE.mod ../module/jj_module.f90 L_MODULE.mod ../module/l_module.f90 W_MODULE.mod ../module/w_module.f90

module/jj_module.obj: A2_MODULE.mod ../module/a2_module.f90 B1_MODULE.mod ../module/b1_module.f90 D.mod ../module/d.f90 L1_MODULE.mod ../module/l1_module.f90 L_MODULE.mod ../module/l_module.f90 M1_MODULE.mod ../module/m1_module.f90 P_MODULE.mod ../module/p_module.f90 V_MODULE.mod ../module/v_module.f90 YYY_MODULE.mod ../module/yyy_module.f90
./JJ_MODULE.mod: module/jj_module.obj A2_MODULE.mod ../module/a2_module.f90 B1_MODULE.mod ../module/b1_module.f90 D.mod ../module/d.f90 L1_MODULE.mod ../module/l1_module.f90 L_MODULE.mod ../module/l_module.f90 M1_MODULE.mod ../module/m1_module.f90 P_MODULE.mod ../module/p_module.f90 V_MODULE.mod ../module/v_module.f90 YYY_MODULE.mod ../module/yyy_module.f90

module/k_module.obj: C1_MODULE.mod ../module/c1_module.f90 JJ_MODULE.mod ../module/jj_module.f90 MG_MODULE.mod ../module/mg_module.f90 UN_MODULE.mod ../module/un_module.f90 YYY_MODULE.mod ../module/yyy_module.f90
./K_MODULE.mod: module/k_module.obj C1_MODULE.mod ../module/c1_module.f90 JJ_MODULE.mod ../module/jj_module.f90 MG_MODULE.mod ../module/mg_module.f90 UN_MODULE.mod ../module/un_module.f90 YYY_MODULE.mod ../module/yyy_module.f90

module/l1_module.obj: A2_MODULE.mod ../module/a2_module.f90 D.mod ../module/d.f90 V_MODULE.mod ../module/v_module.f90
./L1_MODULE.mod: module/l1_module.obj A2_MODULE.mod ../module/a2_module.f90 D.mod ../module/d.f90 V_MODULE.mod ../module/v_module.f90

module/l_module.obj: A2_MODULE.mod ../module/a2_module.f90 D.mod ../module/d.f90
./L_MODULE.mod: module/l_module.obj A2_MODULE.mod ../module/a2_module.f90 D.mod ../module/d.f90

module/m1_module.obj: A2_MODULE.mod ../module/a2_module.f90 B1_MODULE.mod ../module/b1_module.f90 D.mod ../module/d.f90 L1_MODULE.mod ../module/l1_module.f90 L_MODULE.mod ../module/l_module.f90 P_MODULE.mod ../module/p_module.f90 YYY_MODULE.mod ../module/yyy_module.f90
./M1_MODULE.mod: module/m1_module.obj A2_MODULE.mod ../module/a2_module.f90 B1_MODULE.mod ../module/b1_module.f90 D.mod ../module/d.f90 L1_MODULE.mod ../module/l1_module.f90 L_MODULE.mod ../module/l_module.f90 P_MODULE.mod ../module/p_module.f90 YYY_MODULE.mod ../module/yyy_module.f90

module/mf_module.obj: A2_MODULE.mod ../module/a2_module.f90 B2_MODULE.mod ../module/b2_module.f90 CR_MODULE.mod ../module/cr_module.f90 JJ_MODULE.mod ../module/jj_module.f90 K_MODULE.mod ../module/k_module.f90 UN_MODULE.mod ../module/un_module.f90 V_MODULE.mod ../module/v_module.f90 XXX_MODULE.mod ../module/xxx_module.f90 YYY_MODULE.mod ../module/yyy_module.f90
./MF_MODULE.mod: module/mf_module.obj A2_MODULE.mod ../module/a2_module.f90 B2_MODULE.mod ../module/b2_module.f90 CR_MODULE.mod ../module/cr_module.f90 JJ_MODULE.mod ../module/jj_module.f90 K_MODULE.mod ../module/k_module.f90 UN_MODULE.mod ../module/un_module.f90 V_MODULE.mod ../module/v_module.f90 XXX_MODULE.mod ../module/xxx_module.f90 YYY_MODULE.mod ../module/yyy_module.f90

module/mg_module.obj: D.mod ../module/d.f90 UN_MODULE.mod ../module/un_module.f90 YYY_MODULE.mod ../module/yyy_module.f90
./MG_MODULE.mod: module/mg_module.obj D.mod ../module/d.f90 UN_MODULE.mod ../module/un_module.f90 YYY_MODULE.mod ../module/yyy_module.f90

./P_MODULE.mod: module/p_module.obj

module/un_module.obj: A2_MODULE.mod ../module/a2_module.f90
./UN_MODULE.mod: module/un_module.obj A2_MODULE.mod ../module/a2_module.f90

module/v_module.obj: A2_MODULE.mod ../module/a2_module.f90
./V_MODULE.mod: module/v_module.obj A2_MODULE.mod ../module/a2_module.f90

module/w_module.obj: F_MODULE.mod ../module/F_module.f90 A2_MODULE.mod ../module/a2_module.f90 B1_MODULE.mod ../module/b1_module.f90 CBA_MODULE.mod ../module/cba_module.f90 D.mod ../module/d.f90 JJ_MODULE.mod ../module/jj_module.f90 L_MODULE.mod ../module/l_module.f90 V_MODULE.mod ../module/v_module.f90 YYY_MODULE.mod ../module/yyy_module.f90
./W_MODULE.mod: module/w_module.obj F_MODULE.mod ../module/F_module.f90 A2_MODULE.mod ../module/a2_module.f90 B1_MODULE.mod ../module/b1_module.f90 CBA_MODULE.mod ../module/cba_module.f90 D.mod ../module/d.f90 JJ_MODULE.mod ../module/jj_module.f90 L_MODULE.mod ../module/l_module.f90 V_MODULE.mod ../module/v_module.f90 YYY_MODULE.mod ../module/yyy_module.f90

module/xxx_module.obj: B2_MODULE.mod ../module/b2_module.f90 D.mod ../module/d.f90 K_MODULE.mod ../module/k_module.f90 MG_MODULE.mod ../module/mg_module.f90 UN_MODULE.mod ../module/un_module.f90 YYY_MODULE.mod ../module/yyy_module.f90
./XXX_MODULE.mod: module/xxx_module.obj B2_MODULE.mod ../module/b2_module.f90 D.mod ../module/d.f90 K_MODULE.mod ../module/k_module.f90 MG_MODULE.mod ../module/mg_module.f90 UN_MODULE.mod ../module/un_module.f90 YYY_MODULE.mod ../module/yyy_module.f90

module/xyz_module.obj: A2_MODULE.mod ../module/a2_module.f90 D.mod ../module/d.f90 L1_MODULE.mod ../module/l1_module.f90
./XYZ_MODULE.mod: module/xyz_module.obj A2_MODULE.mod ../module/a2_module.f90 D.mod ../module/d.f90 L1_MODULE.mod ../module/l1_module.f90

module/yyy_module.obj: A2_MODULE.mod ../module/a2_module.f90 A_MODULE.mod ../module/a_module.f90 B1_MODULE.mod ../module/b1_module.f90 D.mod ../module/d.f90 L1_MODULE.mod ../module/l1_module.f90 UN_MODULE.mod ../module/un_module.f90 XYZ_MODULE.mod ../module/xyz_module.f90
./YYY_MODULE.mod: module/yyy_module.obj A2_MODULE.mod ../module/a2_module.f90 A_MODULE.mod ../module/a_module.f90 B1_MODULE.mod ../module/b1_module.f90 D.mod ../module/d.f90 L1_MODULE.mod ../module/l1_module.f90 UN_MODULE.mod ../module/un_module.f90 XYZ_MODULE.mod ../module/xyz_module.f90


