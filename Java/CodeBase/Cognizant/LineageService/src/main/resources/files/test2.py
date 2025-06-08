def m_STD_AND_PROCEDURE_TO_FACILITY_main(spark , prg_config , wf_config , mapping_vars , logger , my_reader , my_writer , tr_obj=None, scd_obj=None):
        variable = "EMPLOYEE"
        query = print(variable + """SELECT
                AILNR_DATA,
                FLIGHT_CREW,
                FLIGHT_ID,
                RUN_ID,
                FROM
                """+variable+""".PERF_BID
                WHERE

                ROW_EXPIRY_DT='"""+variable+"""' AND
                (SELECT
                CAST('"""+variable+"""'))""")