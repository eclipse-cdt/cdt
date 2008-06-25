#!/bin/sh

curdir=`pwd`
cd `dirname $0`
mydir=`pwd`
cd "${curdir}"

SIGNED_JAR_SOURCE=${mydir}/eclipse_ext/tm
OUTPUT=${curdir}/output.$$
RESULT=${curdir}/result.$$
TMP=${curdir}/tmp.$$

if [ ! -d ${SIGNED_JAR_SOURCE}/server ]; then
  mkdir ${SIGNED_JAR_SOURCE}/server
  cd ${SIGNED_JAR_SOURCE}/server
  unzip ${curdir}/rseserver-*-signed.zip
fi
cd "${curdir}"

if [ ! -d ${TMP} ]; then
  mkdir -p ${TMP} 
fi
if [ ! -d ${OUTPUT} ]; then
  mkdir -p ${OUTPUT} 
fi
if [ ! -d ${RESULT} ]; then
  mkdir -p ${RESULT} 
fi
for zip in `ls *.zip *.tar` ; do
  cd ${TMP}
  case ${zip} in
    *.zip) unzip -q ${curdir}/${zip} ;;
    *.tar) tar xf ${curdir}/${zip} ;;
  esac
  case ${zip} in
    rseserver*) SIGNED_JARS=${SIGNED_JAR_SOURCE}/server ;;
    *) SIGNED_JARS=${SIGNED_JAR_SOURCE} ;;
  esac
  REF=`find . -name 'epl-v10.html'`
  FILES=`find . -name '*.jar' -o -name 'META-INF'`
  for f in ${FILES} ; do
    printf "${f}: "
    if [ -f ${SIGNED_JARS}/${f} ]; then
      cp -f ${SIGNED_JARS}/${f} ./${f}
      touch -r ${REF} ./${f}
      echo "signed"
    elif [ -d ${SIGNED_JARS}/${f} ]; then
      cp -Rf ${SIGNED_JARS}/${f}/* ${f}
      touch -r ${REF} ${f}/*
      echo "signed"
    else
      echo "."
    fi
  done
  ##cp ${curdir}/${zip} ${OUTPUT}
  case ${zip} in
    *.zip) zip -r -o -q ${OUTPUT}/${zip} * ;;
    *.tar) tar cfv ${OUTPUT}/${zip} * ; touch -r ${REF} ${OUTPUT}/${zip};
  esac
  rm -rf *
  cd ${RESULT}
  case ${zip} in
     rseserver*) mkdir ${zip} ; cd ${zip} ;
        case ${zip} in
           *.zip) unzip -q -o ${OUTPUT}/${zip} ;;
           *.tar) tar xf ${OUTPUT}/${zip} ;;
        esac
        ;;
     *) unzip -q -o ${OUTPUT}/${zip} ;;
  esac
done
rm -rf ${TMP}

echo "--------------------------------------"
echo "DONE"
echo "--------------------------------------"
cd "${curdir}"
echo "MAIN:---------------------------------"
diff -r ${RESULT} ${SIGNED_JAR_SOURCE}
for f in `ls rseserver-*.zip rseserver-*.tar` ; do
  echo "${f}:-----------------------------------"
  diff -r -b ${RESULT}/${f} ${SIGNED_JAR_SOURCE}/server
done

