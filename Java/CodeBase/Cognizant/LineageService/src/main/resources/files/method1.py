
def m_smoketest_infa_complex_main(spark , prg_config , wf_config , mapping_vars , logger , my_reader , my_writer , tr_obj=None, scd_obj=None):



        #Reading Data From Source
        query = tr_obj.df_lkp_demo_source1_query()
        df_lkp_demo_source1 = my_reader.read_database("Oracle", "idwdev", "LKP_DEMO_SOURCE1", query, src_details)
        df_lkp_demo_source1 = df_lkp_demo_source1.withColumn("MONOTONICAL_LOOKUP_ORDER", monotonically_increasing_id())

        #Reading Data From Source
        df_lkp_demo_source3 = my_reader.read_database("Oracle", "idwdev", "LKP_DEMO_SOURCE3", "query", src_details)

        #Reading Data From Source
        query = tr_obj.df_lkp_demo_source2_query()
        df_lkp_demo_source2 = my_reader.read_database("Oracle", "idwdev", "LKP_DEMO_SOURCE2", query, src_details)
        df_lkp_demo_source2 = df_lkp_demo_source2.withColumn("MONOTONICAL_LOOKUP_ORDER", monotonically_increasing_id())

        #Reading Data From Source
        df_demo_source4 = my_reader.read_database("Oracle", "idwdev", "DEMO_SOURCE4", "query", src_details)

        #Expression Transformation
        df_exptrans = tr_obj.get_df_exptrans1(df_demo_source4)

        #Expression Transformation MultiOutputs
        logger.log_info('Expression Tranformation MultiOutputs df_exptransout')
        df_exptransout = df_exptrans.select('ACCT_ID', 'ACCT_TYP', 'O_ACCT_STAT_CD')

        #Lookup Transformation
        df_lkptrans = tr_obj.get_lookup_df_lkptrans(df_lkp_demo_source1, df_exptransout)
        df_lkptrans0 = df_lkptrans

        #Lookup Transformation
        df_lkptrans1 = tr_obj.get_lookup_df_lkptrans1(df_lkp_demo_source2, df_lkptrans0)
        df_lkptrans10 = df_lkptrans1

        #Expression Transformation
        logger.log_info('Expression Tranformation df_exptrans1')
        df_exptrans1 = df_lkptrans10

        #Expression Transformation
        df_exptrans2 = tr_obj.get_df_exptrans21(df_lkp_demo_source3)

        #Expression Transformation MultiOutputs
        logger.log_info('Expression Tranformation MultiOutputs df_exptrans2out')
        df_exptrans2out = df_exptrans2.select('ACCT_ID')

        #Multi Joiner Transformation
        df_jnrtrans = tr_obj.get_multiJoiner_df_jnrtrans(df_exptrans1, df_exptrans2out)

        #Sorter Transformation
        df_srttrans = tr_obj.get_sort_df_srttrans(df_jnrtrans)

        #Calling Persist Function
        df_srttrans = df_srttrans.persist()


        #Router Transformation
        df_rtrtrans0 = tr_obj.get_router_df_rtrtrans0(df_srttrans)

        #Router Transformation
        df_rtrtrans1 = tr_obj.get_router_df_rtrtrans1(df_srttrans)

        #Router Transformation
        df_rtrtrans2 = tr_obj.get_router_df_rtrtrans2(df_srttrans)

        #Aggregator Transformation
        df_aggtrans0 = tr_obj.get_agg_df_aggtrans0(df_rtrtrans0)

        #Expression Transformation
        df_exptrans3 = tr_obj.get_df_exptrans31(df_rtrtrans1)

        #Expression Transformation name change
        logger.log_info('Expression Tranformation name change df_exptrans3out_namechange')
        df_exptrans3_dict = {"o_FIRST_NM" : "FIRST_NM", "o_LAST_NM" : "LAST_NM"}

        df_exptrans3out_namechange = CditUtil.column_rename(df_exptrans3, df_exptrans3_dict)

        #Expression Transformation
        df_exptrans4 = tr_obj.get_df_exptrans41(df_rtrtrans2)

        #Expression Transformation name change
        logger.log_info('Expression Tranformation name change df_exptrans4out_namechange')
        df_exptrans4_dict = {"o_FIRST_NM" : "FIRST_NM", "o_LAST_NM" : "LAST_NM"}

        df_exptrans4out_namechange = CditUtil.column_rename(df_exptrans4, df_exptrans4_dict)

        #Column name change
        logger.log_info('Column name change df_aggtrans0')
        df_aggtrans0_dict = {"o_ACCT_STAT_CD" : "ACCT_STAT_CD", \
                "sum" : "TOTAL_CURR_CRDT_BAL_AMT"}

        df_aggtrans0_in = CditUtil.column_rename(df_aggtrans0, df_aggtrans0_dict)

        #Selecting columns that are propagated to next stage
        logger.log_info('Selecting columns that are propagated to next stage df_aggtrans0')
        df_aggtrans0_out = df_aggtrans0_in.select("ACCT_STAT_CD", "TOTAL_CURR_CRDT_BAL_AMT")

        #Writing data to Target-insert
        my_writer.write_database("Oracle", "idwdev", "DEMO_TARGET_SUM", df_aggtrans0_out, tgt_details, False)

        #Selecting columns that are propagated to next stage
        logger.log_info('Selecting columns that are propagated to next stage df_exptrans4out_namechange')
        df_exptrans4out_namechange_out = df_exptrans4out_namechange.select("CUST_ID", \
                "FIRST_NM", "LAST_NM", "ACCT_ID", "ACCT_TYP")

        #Writing data to Target-insert
        my_writer.write_database("Oracle", "idwdev", "DEMO_TARGET_REVOLVING", df_exptrans4out_namechange_out, tgt_details, False)

        #Selecting columns that are propagated to next stage
        logger.log_info('Selecting columns that are propagated to next stage df_exptrans3out_namechange')
        df_exptrans3out_namechange_out = df_exptrans3out_namechange.select("CUST_ID", \
                "FIRST_NM", "LAST_NM", "ACCT_ID", "ACCT_TYP")

        #Writing data to Target-insert
        my_writer.write_database("Oracle", "idwdev", "DEMO_TARGET_CURRENT", df_exptrans3out_namechange_out, tgt_details, False)

        #Unpersist the dataframe
        df_srttrans = df_srttrans.unpersist()


    except Exception as e:
        logger.log_exception(e, src_details, tgt_details)
    finally:
        logger.log_telemetry(src_details, tgt_details)